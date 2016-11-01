import java.io.{ByteArrayOutputStream, InputStream}
import java.net.InetSocketAddress
import java.util.concurrent.Executors

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}

object App {

  def main(args: Array[String]): Unit = {
    val server = HttpServer.create(new InetSocketAddress("localhost", 5000), 5000)
    val threadPool = Executors.newFixedThreadPool(1)
    server.setExecutor(threadPool)

    // redirect invalid url
    server.createContext("/", new HttpHandler {
      override def handle(exchange: HttpExchange): Unit = {
        exchange.getResponseHeaders.add("Location", "/index/")
        exchange.sendResponseHeaders(301, 0)
        exchange.getResponseBody.close()
      }
    })
    // index
    val url = getClass.getClassLoader.getResource("index.html")
    server.createContext("/index/", new HttpHandler {
      override def handle(exchange: HttpExchange): Unit = {
        val buf = convertByteArray(url.openStream())
        exchange.sendResponseHeaders(200, buf.length)
        exchange.getResponseBody.write(buf)
        exchange.getResponseBody.close()
      }
    })

    server.start()
    println("running...")
  }


  def convertByteArray(is: InputStream): Array[Byte] = {
    val os = new ByteArrayOutputStream()
    // TODO byte固定長で大丈夫なの？
    val buffer = new Array[Byte](1024)
    while (true) {
      val len = is.read(buffer)
      if (0 <= len) {
        os.write(buffer, 0, len)
      } else {
        return os.toByteArray
      }
    }
    // TODO unreachable
    null
  }

}
