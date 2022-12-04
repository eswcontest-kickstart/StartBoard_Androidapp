package com.kickstart.multipleview

import kotlinx.android.synthetic.main.activity_menu.*

class ScaleManager {
    var numDriver: Int = 0
    val threshHoldScale = 5.0
    val threshHoldStableCnt = 10
    var prevScale = 0.0
    var currScale = 0.0
    var stableCnt = 10
    var consecutiveUnstable = 0
    var consecutiveInc = 0
    var consecutiveDec = 0
    var consecutiveStable = 0
    var currWeight:Double = 0.0
    var prevWeight:Double = 0.0
    val threshHoldOneTwoStep = 20
    var oneStep:Boolean = false
    var twoStep:Boolean = false
    var threeStep:Boolean = false
    var fourStep:Boolean = false
    var stable:Boolean = true
    var state = "Stable"

    val scaleList = ArrayList<Double>();

    fun addScale (scale: Double) {
        scaleList.add(scale)

        currScale = scale
        val diff = currScale - prevScale
        if (diff >= -threshHoldScale && diff <= threshHoldScale) {
            stableCnt ++
            consecutiveUnstable --
            if (consecutiveUnstable < 0)
                consecutiveUnstable = 0
            consecutiveStable ++
            if (consecutiveStable > 5)
                consecutiveStable = 5
            consecutiveInc = 0
            consecutiveDec = 0

        }
        else {
            stableCnt--
            consecutiveUnstable ++
            if (consecutiveUnstable > 3)
                consecutiveUnstable = 3
            consecutiveStable --
            if (consecutiveStable < 0 )
                consecutiveStable = 0
            if (diff > 0)
                consecutiveInc ++
            else if (diff < 0)
                consecutiveDec ++
        }

        if (stableCnt >= threshHoldStableCnt || consecutiveStable > 3) {
            stableCnt = threshHoldStableCnt
            stable = true
            if (currScale < 10) {
                oneStep = false
                twoStep = false
                numDriver = 0
                oneStep = false
                twoStep = false
                threeStep = false
                fourStep = false
                currWeight = 0.0
                prevWeight = 0.0
            }
            else {
                if (state == "Unstable") {
                    if (numDriver == 0) {
                        prevWeight = currScale
                        numDriver = 1
                    }
                    else {
                        if (currScale - prevWeight > threshHoldOneTwoStep) {
                            prevWeight = currScale
                            numDriver = 2
                        }
                    }

                    /*
                    if (oneStep == false) {
                        prevWeight = currScale
                        oneStep = true
                    }
                    else if (twoStep == false) {
                        if (currScale - prevWeight > threshHoldOneTwoStep) {
                            prevWeight = currScale
                            twoStep = true
                            numDriver = 1
                        }
                    }
                    else if (threeStep == false) {
                        if (currScale - prevWeight > threshHoldOneTwoStep) {
                            prevWeight = currScale
                            threeStep = true
                            numDriver = 1
                        }
                    }
                    else  {
                        if (currScale - prevWeight > threshHoldOneTwoStep) {
                            prevWeight = currScale
                            fourStep = true
                            numDriver = 2
                        }
                    }

                     */
                }
            }
            state = "Stable"
        }
        // 3회 연속 unstable 탐지
        //if (stableCnt <= threshHoldStableCnt / 3 || consecutiveUnstable >= 3) {
        if (stableCnt <= threshHoldStableCnt / 3 || consecutiveInc >= 2 || consecutiveDec >= 2) {
            stable = false
            state = "Unstable"
        }
        if (stableCnt < 0)
            stableCnt = 0

        prevScale = currScale
    }
    /*
    fun addScale (scale: Double) {
        scaleList.add(scale)

        currScale = scale
        val diff = currScale - prevScale
        if (diff >= -threshHoldScale && diff <= threshHoldScale) {
            stableCnt ++
            consecutiveUnstable --
            if (consecutiveUnstable < 0)
                consecutiveUnstable = 0
            consecutiveStable ++
            if (consecutiveStable > 5)
                consecutiveStable = 5
            consecutiveInc = 0
            consecutiveDec = 0

        }
        else {
            stableCnt--
            consecutiveUnstable ++
            if (consecutiveUnstable > 3)
                consecutiveUnstable = 3
            consecutiveStable --
            if (consecutiveStable < 0 )
                consecutiveStable = 0
            if (diff > 0)
                consecutiveInc ++
            else if (diff < 0)
                consecutiveDec ++
        }

        if (stableCnt >= threshHoldStableCnt || consecutiveStable > 3) {
            stableCnt = threshHoldStableCnt
            stable = true
            if (currScale < 10) {
                oneStep = false
                twoStep = false
                numDriver = 0
                oneStep = false
                twoStep = false
                threeStep = false
                fourStep = false
                currWeight = 0.0
                prevWeight = 0.0
            }
            else {
                if (state == "Unstable") {
                    if (oneStep == false) {
                        prevWeight = currScale
                        oneStep = true
                    }
                    else if (twoStep == false) {
                        if (currScale - prevWeight > threshHoldOneTwoStep) {
                            prevWeight = currScale
                            twoStep = true
                            numDriver = 1
                        }
                    }
                    else if (threeStep == false) {
                        if (currScale - prevWeight > threshHoldOneTwoStep) {
                            prevWeight = currScale
                            threeStep = true
                            numDriver = 1
                        }
                    }
                    else  {
                        if (currScale - prevWeight > threshHoldOneTwoStep) {
                            prevWeight = currScale
                            fourStep = true
                            numDriver = 2
                        }
                    }
                }
            }
            state = "Stable"
        }
        // 3회 연속 unstable 탐지
        //if (stableCnt <= threshHoldStableCnt / 3 || consecutiveUnstable >= 3) {
        if (stableCnt <= threshHoldStableCnt / 3 || consecutiveInc >= 3 || consecutiveDec >= 3) {
            stable = false
            state = "Unstable"
        }
        if (stableCnt < 0)
            stableCnt = 0

        prevScale = currScale
    }
    */


    fun isStable(): Boolean {
        return stable
    }

    fun getNumSteps(): Int {
        if (oneStep == false)
            return 0
        else if (twoStep == false)
            return 1
        else
            return 2
    }


}