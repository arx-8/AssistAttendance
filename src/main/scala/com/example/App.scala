package com.example

import java.io.{BufferedReader, ByteArrayOutputStream, InputStream, InputStreamReader}
import java.net.{InetSocketAddress, URLDecoder}
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.concurrent.Executors

import com.example.controller.{GoogleDriveController, SlackController}
import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}
import org.apache.http.HttpStatus

import scala.util.control.Exception
import scala.util.{Failure, Success}

object App {
  val URL_INDEX = getClass.getClassLoader.getResource("index.html")

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
    server.createContext("/index/", new HttpHandler {
      override def handle(exchange: HttpExchange): Unit = {
        val buf = convertByteArray(URL_INDEX.openStream())
        exchange.sendResponseHeaders(HttpStatus.SC_OK, buf.length)
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
              // 出社
              case "start" => runStart()
              // 退社
              case "quit" => runQuit()
            }
          case None => // none
        }

        redirectToIndex(exchange)
      }
    })

    server.start()
    println(LocalDateTime.now().toString + " server stand up")
  }

  /**
    * 出社の処理。
    * つぶやいて、時間記録
    *
    * @return
    */
  private def runStart() = {
    // Slack
    // TODO 存在しないchName指定されるとHttpServerがタイムアウト
    SlackController.sendMessage("おはようございます。出社しました。")

    // GoogleSpreadsheets
    Exception.allCatch withTry {
      GoogleDriveController.run(true)
    } match {
      case Success(any) => println("出社処理が完了しました。")
      case Failure(t) => println(t.getMessage)
    }
  }

  /**
    * 退社の処理。
    * 時間記録だけ。
    *
    * @return
    */
  private def runQuit() = {
    Exception.allCatch withTry {
      GoogleDriveController.run(false)
    } match {
      case Success(any) => println("退社処理が完了しました。")
      case Failure(t) => println(t.getMessage)
    }
  }

  /**
    * TODO be implicit
    *
    * @param exchange
    */
  private def redirectToIndex(exchange: HttpExchange): Unit = {
    exchange.getResponseHeaders.add("Location", "/index/")
    exchange.sendResponseHeaders(HttpStatus.SC_MOVED_PERMANENTLY, 0)
    exchange.getResponseBody.close()
  }

  private def convertByteArray(is: InputStream): Array[Byte] = {
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

  private def parseQuery(query: String): Map[String, String] = {
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
