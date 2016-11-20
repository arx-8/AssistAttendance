package com.example.controller

import java.io.{File, FileNotFoundException}
import java.util

import com.example.{Consts, Settings}
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.model._
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}

import scala.util.control.Exception._
import scala.util.{Failure, Success}

object GoogleDriveController {
  private val JSON_FACTORY = JacksonFactory.getDefaultInstance.asInstanceOf[JsonFactory]
  private val SCOPES = util.Arrays.asList(SheetsScopes.SPREADSHEETS)
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
      .setServiceAccountScopes(SCOPES)
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

    // TODO フォーマットが月毎に差異が無いので、記入先の位置は計算で導出できそう

    // rangeは決め打ち
    val sheetName = "2016_11" + "!"
    val range = sheetName + "B5:F35"
    allCatch withTry {
      service.spreadsheets().values().get(Settings.googleDrive.spreadsheetId, range).execute()
    } match {
      case Success(resp) =>
        val values = resp.values()
        println(values)

      case Failure(t) => t.printStackTrace()
    }

    // update
    val values = new util.ArrayList[CellData]()
    values.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("HELLO WORLD")))

    allCatch withTry {
      writeCell(service, values)
    } match {
      case Success(resp) =>
        println(resp)

      case Failure(t) => t.printStackTrace()
    }
    println("end")
  }

  private def writeCell(service: Sheets, values: util.List[CellData]) = {
    val requests = new util.ArrayList[Request]()
    requests.add(new Request()
      .setUpdateCells(
        new UpdateCellsRequest()
          .setStart(
            new GridCoordinate()
              .setSheetId(getReportSheetId)
              .setRowIndex(0)
              .setColumnIndex(0)
          )
          .setRows(util.Arrays.asList(new RowData().setValues(values)))
          .setFields("userEnteredValue,userEnteredFormat.backgroundColor")
      )
    )

    val batchUpdateRequest = new BatchUpdateSpreadsheetRequest().setRequests(requests)
    service.spreadsheets().batchUpdate(Settings.googleDrive.spreadsheetId, batchUpdateRequest).execute()
  }

  /**
    * https://docs.google.com/spreadsheets/d/SPREADSHEET_ID/edit#gid={ここがSheetId！}
    * TODO 現状一定みたいだけど、不定なわけがないので、動的に取得しないと
    *
    * @return
    */
  private def getReportSheetId: Int = 6
}
