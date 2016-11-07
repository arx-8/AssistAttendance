import java.io.{BufferedReader, ByteArrayOutputStream, InputStream, InputStreamReader}
import java.net.{InetSocketAddress, URLDecoder}
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
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
        redirectToIndex(exchange)
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

    // index/do
    server.createContext("/index/do", new HttpHandler {
      override def handle(exchange: HttpExchange): Unit = {
        val isr = new InputStreamReader(exchange.getRequestBody, StandardCharsets.UTF_8)
        val br = new BufferedReader(isr)
        val query = br.readLine()
        val params = parseQuery(query)

        params.get("btn") match {
          case Some(value) =>
            value match {
              // TODO 出社
              case "start" => println("this is start")
              // TODO 退社
              case "quit" => println("this is quit")
            }
          case None =>
        }

        redirectToIndex(exchange)
      }
    })

    server.start()
    println(LocalDateTime.now().toString + " server stand up")
  }

  def redirectToIndex(exchange: HttpExchange): Unit = {
    exchange.getResponseHeaders.add("Location", "/index/")
    exchange.sendResponseHeaders(301, 0)
    exchange.getResponseBody.close()
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

  def parseQuery(query: String): Map[String, String] = {
    if (query == null) {
      return Map.empty[String, String]
    }

    val params = Map.newBuilder[String, String]
    val pairs = query.split("[&]")
    pairs
      .filter(1 < _.split("[=]").length)
      .foreach(pair => {
        val param = pair.split("[=]")
        val key = URLDecoder.decode(param(0), System.getProperty("file.encoding"))
        val value = URLDecoder.decode(param(1), System.getProperty("file.encoding"))
        // TODO key重複考慮。とりま必要ないので省略。
        params += (key -> value)
      })
    params.result()
  }
}
