package com.example.util

import java.time.LocalDateTime

import org.scalatest.FunSuite

class ReportDateTimeUtilsTest extends FunSuite {

  test("getThisFiscalMonthsSheetName month 10~12") {
    val dateTime = LocalDateTime.of(2016, 11, 20, 0, 0)
    val r = new ReportDateTimeUtils(dateTime)
    assert(r.getThisFiscalMonthsSheetName == "2016_11")
  }

  test("getThisFiscalMonthsSheetName month 1~9") {
    val dateTime = LocalDateTime.of(2016, 6, 1, 0, 0)
    val r = new ReportDateTimeUtils(dateTime)
    assert(r.getThisFiscalMonthsSheetName == "2016_06")
  }
}
