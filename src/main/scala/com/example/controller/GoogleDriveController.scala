package com.example.controller

import java.io.{File, FileNotFoundException}

import com.example.{Consts, Settings}
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}

import scala.collection.JavaConverters._
import scala.util.control.Exception._
import scala.util.{Failure, Success}

object GoogleDriveController {
  private val JSON_FACTORY = JacksonFactory.getDefaultInstance.asInstanceOf[JsonFactory]
  private val SCOPES = List(SheetsScopes.SPREADSHEETS_READONLY)
  private val HTTP_TRANSPORT = try {
    GoogleNetHttpTransport.newTrustedTransport().asInstanceOf[HttpTransport]
  } catch {
    case t: Throwable =>
      t.printStackTrace()
      sys.exit()
  }

  /**
    * TODO 公式のサンプルコードが動かないので、古い認証方法を使ってる。
    * http://qiita.com/rubytomato@github/items/11c401d581492d3c0361
    *
    * @return
    */
  private def authorize(): Credential = {
    val p12file = new File(Consts.PATH_GOOGLE_DRIVE_P12_KEY_FILE)
    if (!p12file.exists) {
      throw new FileNotFoundException("Failed to load : " + Consts.PATH_GOOGLE_DRIVE_P12_KEY_FILE)
    }

    val credential = new GoogleCredential.Builder()
      .setTransport(HTTP_TRANSPORT)
      .setJsonFactory(JSON_FACTORY)
      .setServiceAccountId(Settings.googleDrive.serviceAccountId)
      .setServiceAccountPrivateKeyFromP12File(p12file)
      .setServiceAccountScopes(SCOPES.asJava)
      .build()

    credential
  }

  private def getSheetsService: Sheets = {
    val credential = authorize()
    new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
      .setApplicationName(Consts.APPLICATION_NAME)
      .build()
  }

  def run() = {
    val service = getSheetsService

    val range = "2016_11!A2:E"
    allCatch withTry {
      service.spreadsheets().values().get(Settings.googleDrive.spreadsheetId, range).execute()
    } match {
      case Success(resp) =>
        val values = resp.values()
        print(values)

      case Failure(t) => t.printStackTrace()
    }
  }
}
