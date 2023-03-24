package com.smartpods.android.pulseecho.Interfaces

import com.smartpods.android.pulseecho.Utilities.Utilities
import kotlin.properties.Delegates

interface PulseVerticalProfile {
    /** BLE STRINGS **/
    var Length: Int
    var Command: Int
    var MoveOneCombo: UInt
    var MoveTwoCombo: UInt
    var MoveThreeCombo: UInt
    var MoveFourCombo: UInt
    var SittingPosHB: UInt
    var SittingPosLB: UInt
    var StandingHB: UInt
    var StandingLB: UInt
    var MinHeightHB: UInt
    var MinHeightLB: UInt
    var MaxHeightHB: UInt
    var MaxHeightLB: UInt
    var DeskHeightOffset: Int
    var DeskOverShoot: Int
    var CRCHighByte: UInt
    var CRCLowByte: UInt
    /** END BLE STRINGS **/

    var movements: List<Map<String, Int>>
    var movement0: String
    var movement1: String
    var movement2: String
    var movement3: String
    var movementRawString: String

    var timeOne: Int
    var timeTwo: Int
    var timeThree: Int
    var timeFour: Int
    var moveOne: Int
    var moveTwo: Int
    var moveThree: Int
    var moveFour: Int

    var SittingPos: Int
    var StandingPos: Int
}

data class ProfileObject(private val data: ByteArray) : PulseVerticalProfile {

    var profileDataBytes = data.map { it.toUByte() }
    var profileData : List<UByte> by Delegates.notNull()

    override var Length: Int = 0
    override var Command: Int = 0
    override var MoveOneCombo: UInt = 0u
    override var MoveTwoCombo: UInt = 0u
    override var MoveThreeCombo: UInt = 0u
    override var MoveFourCombo: UInt = 0u
    override var SittingPosHB: UInt = 0u
    override var SittingPosLB: UInt = 0u
    override var StandingHB: UInt = 0u
    override var StandingLB: UInt = 0u
    override var MinHeightHB: UInt = 0u
    override var MinHeightLB: UInt = 0u
    override var MaxHeightHB: UInt = 0u
    override var MaxHeightLB: UInt = 0u
    override var DeskHeightOffset: Int = 0
    override var DeskOverShoot: Int = 0
    override var CRCHighByte: UInt = 0u
    override var CRCLowByte: UInt = 0u

    override var movements: List<Map<String, Int>> = listOf()
    override var movement0: String = ""
    override var movement1: String = ""
    override var movement2: String = ""
    override var movement3: String = ""
    override var movementRawString: String = ""

    override var timeOne: Int = 0
    override var timeTwo: Int = 0
    override var timeThree: Int = 0
    override var timeFour: Int = 0
    override var moveOne: Int  = 0
    override var moveTwo: Int = 0
    override var moveThree: Int = 0
    override var moveFour: Int = 0

    override var SittingPos: Int = 0
    override var StandingPos: Int = 0

    init {
        this.profileData = this.profileDataBytes

        this.profileData[0].also { this.Length = it.toInt() }
        this.profileData[1].also { this.Command = it.toInt() }
        this.profileData[2].also { this.MoveOneCombo = it.toUInt() }
        this.profileData[3].also { this.MoveTwoCombo = it.toUInt()  }
        this.profileData[4].also { this.MoveThreeCombo = it.toUInt() }
        this.profileData[5].also { this.MoveFourCombo = it.toUInt()  }
        this.profileData[6].also { this.SittingPosHB = it.toUInt()  }
        this.profileData[7].also { this.SittingPosLB = it.toUInt()  }
        this.profileData[8].also { this.StandingHB = it.toUInt()  }
        this.profileData[9].also { this.StandingLB = it.toUInt()  }
        this.profileData[10].also { this.MinHeightHB = it.toUInt()  }
        this.profileData[11].also { this.MinHeightLB = it.toUInt()  }
        this.profileData[12].also { this.MaxHeightHB = it.toUInt()  }
        this.profileData[13].also { this.MaxHeightLB = it.toUInt()  }
        this.profileData[14].also { this.DeskHeightOffset = it.toInt()  }
        this.profileData[15].also { this.DeskOverShoot = it.toInt()  }

        moveOne = this.MoveOneCombo.shr(4).toInt()
        timeOne = (this.MoveOneCombo and 15u).toInt()
        moveTwo = this.MoveTwoCombo.shr(4).toInt()
        timeTwo = (this.MoveTwoCombo and 15u).toInt()
        moveThree = this.MoveThreeCombo.shr(4).toInt()
        timeThree = (this.MoveThreeCombo and 15u).toInt()
        moveFour = this.MoveFourCombo.shr(4).toInt()
        timeFour = (this.MoveFourCombo and 15u).toInt()

        this.SittingPos = Utilities.getCombineByte(this.SittingPosHB.toUByte(), this.SittingPosLB.toUByte())
        this.StandingPos = Utilities.getCombineByte(this.StandingHB.toUByte(), this.StandingLB.toUByte())

        val mMovement0 = if (timeOne != 15 && timeOne != 14)  "${Utilities.getTimeCodes(timeOne)},${this.moveOne}" else ""
        val mMovement1 = if (timeTwo != 15 && timeTwo != 14)  "${Utilities.getTimeCodes(timeTwo)},${this.moveTwo}" else ""
        val mMovement2 = if (timeThree != 15 && timeThree != 14)   "${Utilities.getTimeCodes(timeThree)},${this.moveThree}" else ""
        val mMovement3 = if (timeFour != 15 && timeFour != 14) "${Utilities.getTimeCodes(timeFour)},${this.moveFour}" else ""

        movement0 = mMovement0
        movement1 = mMovement1
        movement2 = mMovement2
        movement3 = mMovement3

        println("mMovement0 : $mMovement0")
        println("mMovement1 : $mMovement1")
        println("mMovement2 : $mMovement2")
        println("mMovement3 : $mMovement3")

        var initialMovement  = this.createMovement(mMovement0)
        val mfirstMovement =  if (initialMovement.containsKey("value")) initialMovement["value"] else 3

        if (mfirstMovement == 3) {

            if (mMovement0 !== "") {
                var movement = createMovement(mMovement0)
                val startAngle = Utilities.getDurationAngle(0 / 60)
                val sweepAngle = Utilities.getSweepAngle((movement["value"]?.toInt() ?: 0) / 60)

                movement += ("start" to startAngle.toInt())
                movement += ("sweep" to sweepAngle.toInt())

                this.movements += movement

            }

            if (mMovement1 !== "") {
                var movement = createMovement(mMovement1)
                val startAngle = Utilities.getDurationAngle((movement["value"]?.toInt() ?: 0) / 60)
                val durationAngle =  (60 -  (movement["value"]?.toInt() ?: 0) / 60)
                val sweepAngle = Utilities.getSweepAngle(durationAngle)

                movement += ("start" to startAngle.toInt())
                movement += ("sweep" to sweepAngle.toInt())

                this.movements += movement
            }

            if (mMovement2 !== "") {
                var movement = createMovement(mMovement2)
                if (mMovement3 !== "") {
                    val nextMove = createMovement(mMovement3)

                    val startAngle = Utilities.getDurationAngle( (movement["value"]?.toInt() ?: 0) / 60)
                    val durationAngle =  (nextMove["value"]?.toInt() ?: 0) -  (movement["value"]?.toInt() ?: 0)
                    val sweepAngle = Utilities.getSweepAngle( durationAngle / 60)

                    movement += ("start" to startAngle.toInt())
                    movement += ("sweep" to sweepAngle.toInt())
                }

                this.movements += movement
            }

            if (mMovement3 !== "") {
                var movement = createMovement(mMovement3)

                val startAngle = Utilities.getDurationAngle( (movement["value"]?.toInt() ?: 0) / 60)
                val sweepAngle = Utilities.getSweepAngle( (3600 - (movement["value"]?.toInt() ?: 0)) / 60)

                movement += ("start" to startAngle.toInt())
                movement += ("sweep" to sweepAngle.toInt())

                this.movements += movement
            }

            println("PULSE PROFILE : ${this.movements}")

        } else {
            if(mMovement0 !== "")  {
                var movement  = createMovement(mMovement0)
                if (mMovement1 !== "") {

                    val startAngle = Utilities.getDurationAngle( 0 / 60)
                    val sweepAngle = Utilities.getSweepAngle( (movement["value"]?.toInt() ?: 0) / 60)

                    movement += ("start" to startAngle.toInt())
                    movement += ("sweep" to sweepAngle.toInt())
                }
                this.movements += movement
            }

            if (mMovement1 !== "") {
                var movement = createMovement(mMovement1)
                if (mMovement2 !== "") {
                    val previousMovement = createMovement(mMovement0)

                    val startAngle = Utilities.getDurationAngle( (previousMovement["value"]?.toInt() ?: 0) / 60)
                    val sweepAngle = Utilities.getSweepAngle( (movement["value"]?.toInt() ?: 0) / 60)

                    movement += ("start" to startAngle.toInt())
                    movement += ("sweep" to sweepAngle.toInt())

                } else {

                    val startAngle = Utilities.getDurationAngle( (movement["value"]?.toInt() ?: 0) / 60)
                    val sweepAngle = Utilities.getSweepAngle( 0 / 60)

                    movement += ("start" to startAngle.toInt())
                    movement += ("sweep" to sweepAngle.toInt())
                }
                this.movements += movement
            }

            if (mMovement2 !== "") {
                var movement = createMovement(mMovement2)
                if (mMovement3 !== "") {
                    val previousMovement  = createMovement(mMovement1)
                    val nextMovement  = createMovement(mMovement3)

                    val mPrevious = previousMovement["value"]?.toInt() ?: 0
                    val mCurrent = movement["value"]?.toInt() ?: 0

                    if (mPrevious == mCurrent) {

                        val startAngle = Utilities.getDurationAngle( (previousMovement["value"]?.toInt() ?: 0) / 60)
                        val sweepAngle = Utilities.getSweepAngle( (nextMovement["value"]?.toInt() ?: 0) / 60)

                        movement += ("start" to startAngle.toInt())
                        movement += ("sweep" to sweepAngle.toInt())
                    } else {

                        val startAngle = Utilities.getDurationAngle( (previousMovement["value"]?.toInt() ?: 0) / 60)
                        val sweepAngle = Utilities.getSweepAngle( (movement["value"]?.toInt() ?: 0) / 60)

                        movement += ("start" to startAngle.toInt())
                        movement += ("sweep" to sweepAngle.toInt())
                    }

                }

                this.movements += movement
            }

            if (mMovement3 !== "") {
                var movement = createMovement(mMovement3)

                val startAngle = Utilities.getDurationAngle( (movement["value"]?.toInt() ?: 0) / 60)
                val sweepAngle = Utilities.getSweepAngle( 0 / 60)

                movement += ("start" to startAngle.toInt())
                movement += ("sweep" to sweepAngle.toInt())

                this.movements += movement
            }

            println("PULSE PROFILE 2: ${this.movements}")
        }

    }

    fun createMovement(movement: String): Map<String, Int> {
        var move = 100
        var moveValue = 0

        val _str = movement.split(",")
        _str[1].also { move = it.toInt() }
        _str[0].also { moveValue = it.toInt() }

        return mapOf("key" to move, "value" to moveValue)
    }
}
