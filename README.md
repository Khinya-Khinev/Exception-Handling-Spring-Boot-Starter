# Spring Boot Exception Handling Starter

## Modules

Provides several extendable modules for web application exception handling:

1. Security
2. Web MVC
3. Validation
4. App exceptions

All modules use ProblemDetailResponse(RFC 9457) that enforce defined by starter contract. Therefore, cannot be extended. Recommended for usage in your custom handlers.

## Starter architecture

![Starter architecture](/docs/starter-arch.excalidraw.png)

## Business Exception usage example

```java
        if (receipt.getStatus() != GoodsReceiptStatus.OPEN) {
            throw AppException.of(ReceivingErrorCode.RECEIPT_INVALID_STATE)
                    .with("expected_status", GoodsReceiptStatus.OPEN)
                    .with("actual_status", receipt.getStatus())
                    .with("receipt_id", receiptId);
        }
```

## Getting started
+ Add dependency (Maven)
```xml
        <dependency>
            <groupId>com.khinya</groupId>
            <artifactId>exception-handling-spring-boot-starter</artifactId>
            <version>0.0.1</version>
        </dependency>
```

+ Define message source in `application.yaml`

```yaml
  messages:
    basename: messages
    encoding: UTF-8
    fallback-to-system-locale: false
    use-code-as-default-message: true
```

## ProblemDetailResponse

A small builder for creating consistent [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) error bodies
(Spring's `ProblemDetail`). The default handlers use it internally, and you can use it in your own handlers
so that every error in your service has the same shape.

### Methods

- `of(status, errorCode, title)`: starts a response. Automatically adds `type` (`about:blank`), `instance` (request path), `error_code` and `timestamp`.
- `detail(String)`: sets the detailed message.
- `type(URI)`: sets a custom problem type.
- `invalidParams(Map<String, String>)`: adds field errors under `invalid_params`.
- `with(key, value)`: adds one custom property. Reserved keys (`type`, `title`, `status`, `detail`, `instance`, `timestamp`, `error_code`, `invalid_params`) are ignored.
- `withAll(Map<String, ?>)`: adds many custom properties.
- `build()`: returns the `ProblemDetail`.

### Example

```java
    @ExceptionHandler(AppException.class)
    protected ProblemDetail handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        String code = errorCode.getCode();
        
        return ProblemDetailResponse
            .of(errorCode.getHttpStatus(),
                code,
                resolveTitle(code))
            .detail("some business exception")    
            .withAll(ex.getErrorContext())
            .build();
    }
```

Response:

```json
    {
      "type": "about:blank",
      "title": "Order not found",
      "status": 404,
      "detail": "some business exception",
      "instance": "/api/v1/orders/42",
      "error_code": "orders.not-found",
      "timestamp": "2026-11-21T14:30:00Z",
      "order_id": 42,
      "expected_status": "pending",
      "actual_status": "closed"
    }
```

