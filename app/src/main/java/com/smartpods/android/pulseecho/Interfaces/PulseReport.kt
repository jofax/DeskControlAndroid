package com.smartpods.android.pulseecho.Interfaces

import kotlin.properties.Delegates

interface PulseReport {
    /** BLE STRINGS **/
    var Length: Int
    var Command: Int
    var ReportingTime: Int
    var YAxisAccRead: Int
    var TemperatureOutput: Int
    var PhotocellReading: Int
    var DbReading: Int
    var SigmaHB: UInt
    var SigmaLB: UInt
    var RowSelectorSitting: Int
    var ColumnSelectorSitting: Int
    var RowSelectorStanding: Int
    var ColumnSelectorStanding: Int
    var CRCHighByte: UInt
    var CRCLowByte: UInt
    /** END BLE STRINGS **/
}

data class ReportObject(private val data: ByteArray) : PulseReport {
    var reportDataBytes = data.map { it.toUByte() }
    var reportData : List<UByte> by Delegates.notNull()

    override var Length: Int = 0
    override var Command: Int  = 0
    override var ReportingTime: Int  = 0
    override var YAxisAccRead: Int  = 0
    override var TemperatureOutput: Int  = 0
    override var PhotocellReading: Int  = 0
    override var DbReading: Int  = 0
    override var SigmaHB: UInt  = 0u
    override var SigmaLB: UInt  = 0u
    override var RowSelectorSitting: Int  = 0
    override var ColumnSelectorSitting: Int  = 0
    override var RowSelectorStanding: Int  = 0
    override var ColumnSelectorStanding: Int  = 0
    override var CRCHighByte: UInt  = 0u
    override var CRCLowByte: UInt = 0u

    init {
        this.reportData = reportDataBytes

        this.reportData[0].also { this.Length = it.toInt() }
        this.reportData[1].also { this.Command = it.toInt() }

        this.reportData[2].also { this.ReportingTime = it.toInt() }
        this.reportData[3].also { this.YAxisAccRead = it.toInt() }
        this.reportData[4].also { this.TemperatureOutput = it.toInt() }
        this.reportData[5].also { this.PhotocellReading = it.toInt() }

        this.reportData[6].also { this.DbReading = it.toInt() }
        this.reportData[7].also { this.SigmaHB = it.toUInt() }
        this.reportData[8].also { this.SigmaLB = it.toUInt() }

        this.reportData[9].also { this.RowSelectorSitting = it.toInt() }
        this.reportData[10].also { this.ColumnSelectorSitting = it.toInt() }
        this.reportData[11].also { this.RowSelectorStanding = it.toInt() }
        this.reportData[12].also { this.ColumnSelectorStanding = it.toInt() }
        this.reportData[13].also { this.CRCHighByte = it.toUInt() }
        this.reportData[14].also { this.CRCLowByte = it.toUInt() }
    }

}