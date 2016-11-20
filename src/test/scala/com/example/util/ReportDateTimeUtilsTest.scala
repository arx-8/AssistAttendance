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

  test("getThisFiscalMonth") {
    var dateTime = LocalDateTime.of(2016, 6, 20, 0, 0)
    var r = new ReportDateTimeUtils(dateTime)
    assert(r.getThisFiscalMonth == 6)

    dateTime = LocalDateTime.of(2016, 11, 21, 0, 0)
    r = new ReportDateTimeUtils(dateTime)
    assert(r.getThisFiscalMonth == 12)
  }

  test("getTimeOfJustToCorrect") {
    var dateTime = LocalDateTime.of(2016, 1, 1, 8, 41)
    var r = new ReportDateTimeUtils(dateTime)
    assert(r.getTimeOfJustToCorrect == "9:00")

    dateTime = LocalDateTime.of(2016, 1, 1, 9, 9)
    r = new ReportDateTimeUtils(dateTime)
    assert(r.getTimeOfJustToCorrect == "9:00")

    dateTime = LocalDateTime.of(2016, 1, 1, 14, 11)
    r = new ReportDateTimeUtils(dateTime)
    assert(r.getTimeOfJustToCorrect == "14:30")
  }

  test("getDayOfFiscalMonth") {
    var dateTime = LocalDateTime.of(2016, 10, 21, 0, 0)
    var r = new ReportDateTimeUtils(dateTime)
    assert(r.getDayOfFiscalMonth == 0)

    dateTime = LocalDateTime.of(2016, 11, 1, 0, 0)
    r = new ReportDateTimeUtils(dateTime)
    assert(r.getDayOfFiscalMonth == 11)

    dateTime = LocalDateTime.of(2016, 11, 20, 0, 0)
    r = new ReportDateTimeUtils(dateTime)
    assert(r.getDayOfFiscalMonth == 30)
  }
}
