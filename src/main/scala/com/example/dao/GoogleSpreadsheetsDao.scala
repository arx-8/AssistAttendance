package com.example.dao

import java.util

import com.example.Settings
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model._

import scala.util.control.Exception
import scala.util.{Failure, Success}


class GoogleSpreadsheetsDao(sheetsService: Sheets) {
  if (sheetsService == null) {
    throw new IllegalArgumentException("null not allowed")
  }

  /**
    * @param range 「SheetName!A4:B5」書式
    * @return
    */
  def readCells(range: String) = {
    Exception.allCatch withTry {
      sheetsService.spreadsheets().values().get(Settings.googleDrive.spreadsheetId, range).execute()
    } match {
      case Success(resp) => Some(resp)
      case Failure(t) =>
        t.printStackTrace()
        None
    }
  }

  /**
    * とりあえず1セルだけ書ける
    *
    * @param writeVal
    * @param coord
    * @return
    */
  def writeCell(writeVal: CellData, coord: GridCoordinate) = {
    val requests = util.Arrays.asList(
      new Request().setUpdateCells(
        new UpdateCellsRequest()
          .setStart(coord)
          .setRows(util.Arrays.asList(new RowData().setValues(util.Arrays.asList(writeVal))))
          // TODO 書式指定できそう？ https://developers.google.com/sheets/samples/formatting
          .setFields("userEnteredValue,userEnteredFormat.numberFormat")
      )
    )
    val batchUpReq = new BatchUpdateSpreadsheetRequest().setRequests(requests)
    sheetsService.spreadsheets().batchUpdate(Settings.googleDrive.spreadsheetId, batchUpReq).execute()
  }
}
