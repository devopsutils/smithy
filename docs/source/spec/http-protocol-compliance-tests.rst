.. _http-protocol-compliance-tests:

==============================
HTTP Protocol Compliance Tests
==============================

Smithy is a protocol-agnostic IDL that tries to abstract the serialization
format of request and response messages sent between a client and server.
Protocol specifications define the serialization format of a protocol, and
protocol compliance tests help to ensure that implementations correctly
implement a protocol specification.

.. contents:: Table of contents
    :depth: 2
    :local:
    :backlinks: none


--------
Overview
--------

This specification defines two traits in the ``smithy.test`` namespace that
are used to make assertions about client and server protocol implementations.

:ref:`smithy.test#httpRequestTests <httpRequestTests-trait>`
    Used to define how an HTTP request is serialized given a specific
    protocol, authentication scheme, and set of input parameters.
:ref:`smithy.test#httpResponseTests <httpResponseTests-trait>`
   Used to define how an HTTP response is serialized given a specific
   protocol, authentication scheme, and set of output or error parameters.

Protocol implementation developers use these traits to ensure that their
implementation is correct. This can be done through code generation of test
cases or by dynamically loading test cases at runtime. For example, a Java
implementation could generate JUnit test cases to assert that the
expectations defined in a model match the behavior of a generated client
or server.


Parameter format
================

The ``params`` property used in both the ``httpRequestTests`` trait and
``httpResponseTests`` trait test cases represents parameters that are used
to serialize HTTP requests and responses. In order to compare implementation
specific results against the expected result of each test case across
different programming languages, parameters are defined in the same format
specified in :ref:`trait-definition-values` with the following additional
constraints:

* Timestamp values must be converted to a Unix timestamp represented
  as an integer.
* Client implementations that automatically provide values for members marked
  with the :ref:`idempotencyToken-trait` MUST use a constant value of
  ``00000000-0000-4000-8000-000000000000``.


.. _httpRequestTests-trait:

----------------
httpRequestTests
----------------

Summary
    The ``httpRequestTests`` trait is used to define how an HTTP request is
    serialized given a specific protocol, authentication scheme, and set of
    input parameters.
Trait selector
    .. code-block:: css

        operation
Value type
    ``list`` of ``HttpRequestTestCase`` structures

The ``httpRequestTests`` trait is a list of ``HttpRequestTestCase`` structures
that support the following members:

.. list-table::
    :header-rows: 1
    :widths: 10 25 65

    * - Property
      - Type
      - Description
    * - id
      - ``string``
      - **Required**. The identifier of the test case. This identifier can
        be used by protocol test implementations to filter out unsupported
        test cases by ID, to generate test case names, etc. The provided
        ``id`` MUST match Smithy's :token:`identifier` ABNF. No two
        ``httpRequestTests`` test cases can share the same ID.
    * - protocol
      - ``string``
      - **Required**. The name of the :ref:`protocol <protocols-trait>` to
        test. Because Smithy services can support multiple protocols, each
        test MUST specify which protocol is under test.
    * - method
      - ``string``
      - **Required**. The expected serialized HTTP request method.
    * - uri
      - ``string``
      - **Required**. The request-target of the HTTP request, not including
        the query string (for example, "/foo/bar").
    * - authScheme
      - ``string``
      - The optional :ref:`authentication scheme <auth-trait>` to assume.
        It's possible that specific authentication schemes might influence
        the serialization logic of an HTTP request.
    * - queryParams
      - ``list<string>``
      - A list of the expected serialized query string parameters.

        Each element in the list is a query string key value pair
        that starts with the query string parameter name optionally
        followed by "=", optionally followed by the query string
        parameter value. For example, "foo=bar", "foo=", and "foo"
        are all valid values.

        .. note::

            This kind of list is used instead of a map so that query string
            parameter values for lists can be represented using repeated
            key-value pairs.

        The query string parameter name and the value MUST appear in the
        format in which it is expected to be sent over the wire; if a key or
        value needs to be percent-encoded, then it MUST appear
        percent-encoded in this list.

        A serialized HTTP request is not in compliance with the protocol
        if any query string parameter defined in ``queryParams`` is not
        defined in the request or if the value of a query string parameter
        in the request differs from the expected value.

        ``queryParams`` applies no constraints on additional query parameters.
    * - forbidQueryParams
      - ``list<string>``
      - A list of query string parameter names that must not appear in the
        serialized HTTP request.

        Each value MUST appear in the format in which it is sent over the
        wire; if a key needs to be percent-encoded, then it MUST appear
        percent-encoded in this list.
    * - requireQueryParams
      - ``list<string>``
      - A list of query string parameter names that MUST appear in the
        serialized request URI, but no assertion is made on the value.

        Each value MUST appear in the format in which it is sent over the
        wire; if a key needs to be percent-encoded, then it MUST appear
        percent-encoded in this list.
    * - headers
      - ``map<string, string>``
      - A map of expected HTTP headers. Each key represents a header field
        name and each value represents the expected header value. An HTTP
        request is not in compliance with the protocol if any listed header
        is missing from the serialized request or if the expected header
        value differs from the serialized request value.

        ``headers`` applies no constraints on additional headers.
    * - forbidHeaders
      - [``string``]
      - A list of header field names that must not appear in the serialized
        HTTP request.
    * - requireHeaders
      - [``string``]
      - A list of header field names that must appear in the serialized
        HTTP message, but no assertion is made on the value. Headers listed
        in ``headers`` do not need to appear in this list.
    * - body
      - ``string``
      - The expected HTTP message body. If no request body is defined,
        then no assertions are made about the body of the message.
    * - bodyMediaType
      - ``string``
      - The media type of the ``body``. This is used to help test runners
        to parse and validate the expected data against generated data.
        Binary media type formats require that the contents of ``body`` are
        base64 encoded.
    * - params
      - ``document``
      - Defines the input parameters used to generate the HTTP request. These
        parameters MUST be compatible with the input of the operation.
    * - vendorParams
      - ``document``
      - Defines vendor-specific parameters that are used to influence the
        request. For example, some vendors might utilize environment
        variables, configuration files on disk, or other means to influence
        the serialization formats used by clients or servers.
    * - documentation
      - ``string``
      - A description of the test and what is being asserted defined in
        CommonMark_.


HTTP request example
====================

The following example defines a protocol compliance test for a JSON protocol
that uses :ref:`HTTP binding traits <http-traits>`.

.. tabs::

    .. code-tab:: smithy

        namespace smithy.example

        use smithy.test#httpRequestTests

        @http(method: "POST", uri: "/")
        @httpRequestTests([
            {
                id: "say_hello",
                protocol: "example",
                params: {
                    "greeting": "Hi",
                    "name": "Teddy",
                    "query": "Hello there"
                },
                method: "POST",
                uri: "/",
                queryParams: [
                    "Hi=Hello%20there"
                ],
                headers: {
                    "X-Greeting": "Hi",
                },
                body: "{\"name\": \"Teddy\"}",
                bodyMediaType: "application/json"
            }
        ])
        operation SayHello {
            input: SayHelloInput
        }

        structure SayHelloInput {
            @httpHeader("X-Greeting")
            greeting: String,

            @httpQuery("Hi")
            query: String,

            name: String
        }

    .. code-tab:: json

        {
            "smithy": "0.5.0",
            "shapes": {
                "smithy.example#SayHello": {
                    "type": "operation",
                    "input": {
                        "target": "smithy.example#SayHelloInput"
                    },
                    "traits": {
                        "smithy.api#http": {
                            "method": "POST",
                            "uri": "/",
                            "code": 200
                        },
                        "smithy.test#httpRequestTests": [
                            {
                                "id": "say_hello",
                                "protocol": "example",
                                "method": "POST",
                                "uri": "/",
                                "headers": {
                                    "X-Greeting": "Hi"
                                },
                                "queryParams": [
                                    "Hi=Hello%20there"
                                ],
                                "body": "{\"name\": \"Teddy\"}",
                                "bodyMediaType": "application/json"
                                "params": {
                                    "greeting": "Hi",
                                    "name": "Teddy",
                                    "query": "Hello there"
                                }
                            }
                        ]
                    }
                },
                "smithy.example#SayHelloInput": {
                    "type": "structure",
                    "members": {
                        "greeting": {
                            "target": "smithy.api#String",
                            "traits": {
                                "smithy.api#httpHeader": "X-Greeting"
                            }
                        },
                        "name": {
                            "target": "smithy.api#String"
                        }
                    }
                }
            }
        }


.. _httpResponseTests-trait:

-----------------
httpResponseTests
-----------------

Summary
    The ``httpResponseTests`` trait is used to define how an HTTP response
    is serialized given a specific protocol, authentication scheme, and set
    of output or error parameters.
Trait selector
    .. code-block:: css

        :each(operation, structure[trait|error])
Value type
    ``list`` of ``HttpResponseTestCase`` structures

The ``httpResponseTests`` trait is a list of ``HttpResponseTestCase``
structures that support the following members:

.. list-table::
    :header-rows: 1
    :widths: 10 25 65

    * - Property
      - Type
      - Description
    * - id
      - ``string``
      - **Required**. The identifier of the test case. This identifier can
        be used by protocol test implementations to filter out unsupported
        test cases by ID, to generate test case names, etc. The provided
        ``id`` MUST match Smithy's :token:`identifier` ABNF. No two
        ``httpResponseTests`` test cases can share the same ID.
    * - protocol
      - ``string``
      - **Required**. The name of the :ref:`protocol <protocols-trait>` to
        test. Because Smithy services can support multiple protocols, each
        test MUST specify which protocol is under test.
    * - code
      - ``integer``
      - **Required**. The expected HTTP response status code.
    * - authScheme
      - ``string``
      - The optional :ref:`authentication scheme <auth-trait>` to assume.
        It's possible that specific authentication schemes might influence
        the serialization logic of an HTTP response.
    * - headers
      - ``map<string, string>``
      - A map of expected HTTP headers. Each key represents a header field
        name and each value represents the expected header value. An HTTP
        response is not in compliance with the protocol if any listed header
        is missing from the serialized response or if the expected header
        value differs from the serialized response value.

        ``headers`` applies no constraints on additional headers.
    * - forbidHeaders
      - ``list<string>``
      - A list of header field names that must not appear in the serialized
        HTTP response.
    * - requireHeaders
      - ``list<string>``
      - A list of header field names that must appear in the serialized
        HTTP response, but no assertion is made on the value. Headers listed
        in ``headers`` do not need to appear in this list.
    * - body
      - ``string``
      - The expected HTTP message body. If no response body is defined,
        then no assertions are made about the body of the message.
    * - bodyMediaType
      - ``string``
      - The media type of the ``body``. This is used to help test runners
        to parse and validate the expected data against generated data.
        Binary media type formats require that the contents of ``body`` are
        base64 encoded.
    * - params
      - ``document``
      - Defines the output or error parameters used to generate the HTTP
        response. These parameters MUST be compatible with the targeted
        operation's output or the targeted error structure.
    * - vendorParams
      - ``document``
      - Defines vendor-specific parameters that are used to influence the
        response. For example, some vendors might utilize environment
        variables, configuration files on disk, or other means to influence
        the serialization formats used by clients or servers.
    * - documentation
      - ``string``
      - A description of the test and what is being asserted defined in
        CommonMark_.


HTTP response example
=====================

The following example defines a protocol compliance test for a JSON protocol
that uses :ref:`HTTP binding traits <http-traits>`.

.. tabs::

    .. code-tab:: smithy

        namespace smithy.example

        use smithy.test#httpResponseTests

        @http(method: "POST", uri: "/")
        @httpResponseTests([
            {
                id: "say_goodbye",
                protocol: "example",
                params: {farewell: "Bye"},
                code: 200,
                headers: {
                    "X-Farewell": "Bye",
                    "Content-Length": "0"
                }
            }
        ])
        operation SayGoodbye {
            output: SayGoodbyeOutput
        }

        structure SayGoodbyeOutput {
            @httpHeader("X-Farewell")
            farewell: String,
        }

    .. code-tab:: json

        {
            "smithy": "0.5.0",
            "shapes": {
                "smithy.example#SayGoodbye": {
                    "type": "operation",
                    "output": {
                        "target": "smithy.example#SayGoodbyeOutput"
                    },
                    "traits": {
                        "smithy.api#http": {
                            "method": "POST",
                            "uri": "/",
                            "code": 200
                        },
                        "smithy.test#httpResponseTests": [
                            {
                                "id": "say_goodbye",
                                "protocol": "example",
                                "headers": {
                                    "Content-Length": "0",
                                    "X-Farewell": "Bye"
                                },
                                "params": {
                                    "farewell": "Bye"
                                },
                                "code": 200
                            }
                        ]
                    }
                },
                "smithy.example#SayGoodbyeOutput": {
                    "type": "structure",
                    "members": {
                        "farewell": {
                            "target": "smithy.api#String",
                            "traits": {
                                "smithy.api#httpHeader": "X-Farewell"
                            }
                        }
                    }
                }
            }
        }


HTTP error response example
===========================

The ``httpResponseTests`` trait can be applied to error structures to define
how an error HTTP response is serialized. Client protocol compliance test
implementations SHOULD ensure that each error with the ``httpResponseTests``
trait associated with an operation can be properly deserialized.

The following example defines a protocol compliance test for a JSON protocol
that uses :ref:`HTTP binding traits <http-traits>`.

.. tabs::

    .. code-tab:: smithy

        namespace smithy.example

        use smithy.test#httpResponseTests

        @error("client")
        @httpError(400)
        @httpResponseTests([
            {
                id: "invalid_greeting",
                protocol: "example",
                params: {foo: "baz", message: "Hi"},
                code: 400,
                headers: {"X-Foo": "baz"},
                body: "{\"message\": \"Hi\"}",
                bodyMediaType: "application/json",
            }
        ])
        structure InvalidGreeting {
            @httpHeader("X-Foo")
            foo: String,

            message: String,
        }

    .. code-tab:: json

        {
            "smithy": "0.5.0",
            "shapes": {
                "smithy.example#InvalidGreeting": {
                    "type": "structure",
                    "members": {
                        "foo": {
                            "target": "smithy.api#String",
                            "traits": {
                                "smithy.api#httpHeader": "X-Foo"
                            }
                        },
                        "message": {
                            "target": "smithy.api#String"
                        }
                    },
                    "traits": {
                        "smithy.api#error": "client",
                        "smithy.api#httpError": 400,
                        "smithy.test#httpResponseTests": [
                            {
                                "id": "invalid_greeting",
                                "protocol": "example",
                                "body": "{\"message\": \"Hi\"}",
                                "bodyMediaType": "application/json",
                                "headers": {
                                    "X-Foo": "baz"
                                },
                                "params": {
                                    "foo": "baz",
                                    "message": "Hi"
                                },
                                "code": 400
                            }
                        ]
                    }
                }
            }
        }


.. _CommonMark: https://spec.commonmark.org/
