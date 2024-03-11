# HTTP Cache Feature

This feature enables support for server-side [HTTP Caching] (https://developer.mozilla.org/en-US/docs/Web/HTTP/Caching) 
through the use of HTTP response headers.
Actual caching is expected to be provided by third parties:
* Proxy or Gateway
* Network Appliance
* CDN

## CGlobal Support
Enabling the feature automates setting the following response headers:
* Etag - entity fingerprint used to detect changes to the entity
* Last-Modified - data/time when data was last changed. If the response is for a single entity, the value is derived from the entity metadata, otherwise the current date/time is used

## Annotation Based Support
Other response header settings can be enabled on a per-method basis through the use of annotations:
* Cache-Control - the value can be configured using the `@CacheControl` annotation
* Expires - the value can be configured using the `@Expires` annotation

## Future Considerations

* Automate CacheControl.private when a resource method is annotated and the response is expected to be different depending on whether the requesting user is the resource owner or not
