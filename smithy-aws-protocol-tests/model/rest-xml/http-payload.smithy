// This file defines test cases that test HTTP payload bindings.
// See: https://awslabs.github.io/smithy/spec/http.html#httppayload-trait

$version: "0.5.0"

namespace aws.protocols.tests.restxml

use aws.protocols.tests.shared#TextPlainBlob
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests

/// This examples serializes a blob shape in the payload.
///
/// In this example, no XML document is synthesized because the payload is
/// not a structure or a union type.
@http(uri: "/HttpPayloadTraits", method: "POST")
operation HttpPayloadTraits {
    input: HttpPayloadTraitsInputOutput,
    output: HttpPayloadTraitsInputOutput
}

apply HttpPayloadTraits @httpRequestTests([
    {
        id: "HttpPayloadTraitsWithBlob",
        documentation: "Serializes a blob in the HTTP payload",
        protocol: "aws.rest-xml",
        method: "POST",
        uri: "/HttpPayloadTraits",
        body: "blobby blob blob",
        headers: {
            "X-Foo": "Foo"
        },
        params: {
            foo: "Foo",
            blob: "blobby blob blob"
        }
    },
    {
        id: "HttpPayloadTraitsWithNoBlobBody",
        documentation: "Serializes an empty blob in the HTTP payload",
        protocol: "aws.rest-xml",
        method: "POST",
        uri: "/HttpPayloadTraits",
        body: "",
        headers: {
            "X-Foo": "Foo"
        },
        params: {
            foo: "Foo"
        }
    },
])

apply HttpPayloadTraits @httpResponseTests([
    {
        id: "HttpPayloadTraitsWithBlob",
        documentation: "Serializes a blob in the HTTP payload",
        protocol: "aws.rest-xml",
        code: 200,
        body: "blobby blob blob",
        headers: {
            "X-Foo": "Foo"
        },
        params: {
            foo: "Foo",
            blob: "blobby blob blob"
        }
    },
    {
        id: "HttpPayloadTraitsWithNoBlobBody",
        documentation: "Serializes an empty blob in the HTTP payload",
        protocol: "aws.rest-xml",
        code: 200,
        body: "",
        headers: {
            "X-Foo": "Foo"
        },
        params: {
            foo: "Foo"
        }
    }
])

structure HttpPayloadTraitsInputOutput {
    @httpHeader("X-Foo")
    foo: String,

    @httpPayload
    blob: Blob,
}

/// This examples uses a `@mediaType` trait on the payload to force a custom
/// content-type to be serialized.
@http(uri: "/HttpPayloadTraitsWithMediaType", method: "POST")
operation HttpPayloadTraitsWithMediaType {
    input: HttpPayloadTraitsWithMediaTypeInputOutput,
    output: HttpPayloadTraitsWithMediaTypeInputOutput
}

apply HttpPayloadTraitsWithMediaType @httpRequestTests([
    {
        id: "HttpPayloadTraitsWithMediaTypeWithBlob",
        documentation: "Serializes a blob in the HTTP payload with a content-type",
        protocol: "aws.rest-xml",
        method: "POST",
        uri: "/HttpPayloadTraitsWithMediaType",
        body: "blobby blob blob",
        headers: {
            "X-Foo": "Foo",
            "Content-Type": "text/plain"
        },
        params: {
            foo: "Foo",
            blob: "blobby blob blob"
        }
    }
])

apply HttpPayloadTraitsWithMediaType @httpResponseTests([
    {
        id: "HttpPayloadTraitsWithMediaTypeWithBlob",
        documentation: "Serializes a blob in the HTTP payload with a content-type",
        protocol: "aws.rest-xml",
        code: 200,
        body: "blobby blob blob",
        headers: {
            "X-Foo": "Foo",
            "Content-Type": "text/plain"
        },
        params: {
            foo: "Foo",
            blob: "blobby blob blob"
        }
    }
])

structure HttpPayloadTraitsWithMediaTypeInputOutput {
    @httpHeader("X-Foo")
    foo: String,

    @httpPayload
    blob: TextPlainBlob,
}

/// This examples serializes a structure in the payload.
///
/// Note that serializing a structure changes the wrapper element name
/// to match the targeted structure.
@idempotent
@http(uri: "/HttpPayloadWithStructure", method: "PUT")
operation HttpPayloadWithStructure {
    input: HttpPayloadWithStructureInputOutput,
    output: HttpPayloadWithStructureInputOutput
}

apply HttpPayloadWithStructure @httpRequestTests([
    {
        id: "HttpPayloadWithStructure",
        documentation: "Serializes a structure in the payload",
        protocol: "aws.rest-xml",
        method: "PUT",
        uri: "/HttpPayloadWithStructure",
        body: """
              <NestedPayload>
                  <greeting>hello</greeting>
                  <name>Phreddy</name>
              </NestedPayload>
              """,
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            nested: {
                greeting: "hello",
                name: "Phreddy"
            }
        }
    }
])

apply HttpPayloadWithStructure @httpResponseTests([
    {
        id: "HttpPayloadWithStructure",
        documentation: "Serializes a structure in the payload",
        protocol: "aws.rest-xml",
        code: 200,
        body: """
            <NestedPayload>
                <greeting>hello</greeting>
                <name>Phreddy</name>
            </NestedPayload>
            """,
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            nested: {
                greeting: "hello",
                name: "Phreddy"
            }
        }
    }
])

structure HttpPayloadWithStructureInputOutput {
    @httpPayload
    nested: NestedPayload,
}

structure NestedPayload {
    greeting: String,
    name: String,
}

/// The following example serializes a payload that uses an XML name,
/// changing the wrapper name.
@idempotent
@http(uri: "/HttpPayloadWithXmlName", method: "PUT")
operation HttpPayloadWithXmlName {
    input: HttpPayloadWithXmlNameInputOutput,
    output: HttpPayloadWithXmlNameInputOutput
}

apply HttpPayloadWithXmlName @httpRequestTests([
    {
        id: "HttpPayloadWithXmlName",
        documentation: "Serializes a structure in the payload using a wrapper name based on xmlName",
        protocol: "aws.rest-xml",
        method: "PUT",
        uri: "/HttpPayloadWithXmlName",
        body: "<Hello><name>Phreddy</name></Hello>",
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            nested: {
                name: "Phreddy"
            }
        }
    }
])

apply HttpPayloadWithXmlName @httpResponseTests([
    {
        id: "HttpPayloadWithXmlName",
        documentation: "Serializes a structure in the payload using a wrapper name based on xmlName",
        protocol: "aws.rest-xml",
        code: 200,
        body: "<Hello><name>Phreddy</name></Hello>",
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            nested: {
                name: "Phreddy"
            }
        }
    }
])

structure HttpPayloadWithXmlNameInputOutput {
    @httpPayload
    nested: PayloadWithXmlName,
}

@xmlName("Hello")
structure PayloadWithXmlName {
    name: String
}

/// The following example serializes a payload that uses an XML namespace.
@idempotent
@http(uri: "/HttpPayloadWithXmlNamespace", method: "PUT")
operation HttpPayloadWithXmlNamespace {
    input: HttpPayloadWithXmlNamespaceInputOutput,
    output: HttpPayloadWithXmlNamespaceInputOutput
}

apply HttpPayloadWithXmlNamespace @httpRequestTests([
    {
        id: "HttpPayloadWithXmlNamespace",
        documentation: "Serializes a structure in the payload using a wrapper with an XML namespace",
        protocol: "aws.rest-xml",
        method: "PUT",
        uri: "/HttpPayloadWithXmlNamespace",
        body: """
              <PayloadWithXmlNamespace xmlns="http://foo.com">
                  <name>Phreddy</name>
              </PayloadWithXmlNamespace>""",
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            nested: {
                name: "Phreddy"
            }
        }
    }
])

apply HttpPayloadWithXmlNamespace @httpResponseTests([
    {
        id: "HttpPayloadWithXmlNamespace",
        documentation: "Serializes a structure in the payload using a wrapper with an XML namespace",
        protocol: "aws.rest-xml",
        code: 200,
        body: """
              <PayloadWithXmlNamespace xmlns="http://foo.com">
                  <name>Phreddy</name>
              </PayloadWithXmlNamespace>""",
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            nested: {
                name: "Phreddy"
            }
        }
    }
])

structure HttpPayloadWithXmlNamespaceInputOutput {
    @httpPayload
    nested: PayloadWithXmlNamespace,
}

@xmlNamespace(uri: "http://foo.com")
structure PayloadWithXmlNamespace {
    name: String
}

/// The following example serializes a payload that uses an XML namespace.
@idempotent
@http(uri: "/HttpPayloadWithXmlNamespaceAndPrefix", method: "PUT")
operation HttpPayloadWithXmlNamespaceAndPrefix {
    input: HttpPayloadWithXmlNamespaceAndPrefixInputOutput,
    output: HttpPayloadWithXmlNamespaceAndPrefixInputOutput
}

apply HttpPayloadWithXmlNamespaceAndPrefix @httpRequestTests([
    {
        id: "HttpPayloadWithXmlNamespaceAndPrefix",
        documentation: "Serializes a structure in the payload using a wrapper with an XML namespace",
        protocol: "aws.rest-xml",
        method: "PUT",
        uri: "/HttpPayloadWithXmlNamespaceAndPrefix",
        body: """
              <PayloadWithXmlNamespaceAndPrefix xmlns:baz="http://foo.com">
                  <name>Phreddy</name>
              </PayloadWithXmlNamespaceAndPrefix>""",
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            nested: {
                name: "Phreddy"
            }
        }
    }
])

apply HttpPayloadWithXmlNamespaceAndPrefix @httpResponseTests([
    {
        id: "HttpPayloadWithXmlNamespaceAndPrefix",
        documentation: "Serializes a structure in the payload using a wrapper with an XML namespace",
        protocol: "aws.rest-xml",
        code: 200,
        body: """
              <PayloadWithXmlNamespaceAndPrefix xmlns:baz="http://foo.com">
                  <name>Phreddy</name>
              </PayloadWithXmlNamespaceAndPrefix>""",
        bodyMediaType: "application/xml",
        headers: {
            "Content-Type": "application/xml"
        },
        params: {
            nested: {
                name: "Phreddy"
            }
        }
    }
])

structure HttpPayloadWithXmlNamespaceAndPrefixInputOutput {
    @httpPayload
    nested: PayloadWithXmlNamespaceAndPrefix,
}

@xmlNamespace(uri: "http://foo.com", prefix: "baz")
structure PayloadWithXmlNamespaceAndPrefix {
    name: String
}
