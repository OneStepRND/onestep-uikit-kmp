import Foundation

/// Intercepts all URLSession traffic and logs full request/response details.
class NetworkLogger: URLProtocol {

    private static let handledKey = "NetworkLoggerHandled"

    static func startLogging() {
        URLProtocol.registerClass(NetworkLogger.self)
    }

    override class func canInit(with request: URLRequest) -> Bool {
        guard property(forKey: handledKey, in: request) == nil else {
            return false
        }
        return true
    }

    override class func canonicalRequest(for request: URLRequest) -> URLRequest {
        return request
    }

    override func startLoading() {
        let request = self.request

        // Log request
        print("[NET] ➡️ \(request.httpMethod ?? "?") \(request.url?.absoluteString ?? "?")")
        if let headers = request.allHTTPHeaderFields {
            print("[NET]   Request Headers: \(headers)")
        }
        if let body = request.httpBody, let bodyStr = String(data: body, encoding: .utf8) {
            print("[NET]   Request Body: \(bodyStr)")
        } else if let stream = request.httpBodyStream {
            let data = readStream(stream)
            if let bodyStr = String(data: data, encoding: .utf8) {
                print("[NET]   Request Body (stream): \(bodyStr)")
            }
        }

        // Tag the request so we don't intercept it again
        let mutableRequest = (request as NSURLRequest).mutableCopy() as! NSMutableURLRequest
        URLProtocol.setProperty(true, forKey: Self.handledKey, in: mutableRequest)

        let session = URLSession(configuration: .default, delegate: nil, delegateQueue: nil)
        let task = session.dataTask(with: mutableRequest as URLRequest) { [weak self] data, response, error in
            guard let self else { return }

            // Log response
            if let httpResponse = response as? HTTPURLResponse {
                print("[NET] ⬅️ \(httpResponse.statusCode) \(request.url?.absoluteString ?? "?")")
                print("[NET]   Response Headers: \(httpResponse.allHeaderFields)")
            }
            if let data, let bodyStr = String(data: data, encoding: .utf8) {
                print("[NET]   Response Body: \(bodyStr)")
            }

            // Forward to original caller
            if let response {
                self.client?.urlProtocol(self, didReceive: response, cacheStoragePolicy: .notAllowed)
            }
            if let data {
                self.client?.urlProtocol(self, didLoad: data)
            }
            if let error {
                self.client?.urlProtocol(self, didFailWithError: error)
            } else {
                self.client?.urlProtocolDidFinishLoading(self)
            }
        }
        task.resume()
    }

    override func stopLoading() {}

    private func readStream(_ stream: InputStream) -> Data {
        stream.open()
        var data = Data()
        let buffer = UnsafeMutablePointer<UInt8>.allocate(capacity: 4096)
        defer { buffer.deallocate() }
        while stream.hasBytesAvailable {
            let bytesRead = stream.read(buffer, maxLength: 4096)
            if bytesRead > 0 {
                data.append(buffer, count: bytesRead)
            }
        }
        stream.close()
        return data
    }
}
