package blockchain

import java.security.MessageDigest
import kotlin.random.Random

data class Block(val n: Int, val id: Int, val time: Long, val hashPrev: String, val nameThread: String, val data: String) {
    private val magic = magic("$id $time $hashPrev $data", n)
    val hash = applySha256("$id $time $hashPrev $data $magic")
    val timeGen = (System.currentTimeMillis() - time) / 1000
    var optionN = ""

    override fun toString(): String {
        return """Block:
Created by miner # $nameThread
Id: $id
Timestamp: $time
Magic number: $magic
Hash of the previous block:
$hashPrev
Hash of the block:
$hash
Block data: $data
Block was generating for $timeGen seconds
$optionN"""
    }
}

fun magic(str: String, n: Int): String {
    var hash: String
    var magic = 0
    var bool = true
    l@while (bool) {
        magic = Random.nextInt(999999999)
        hash = applySha256("$str $magic")
        for (i in 0 until n) {
            if (hash[i] != '0') continue@l
        }
        bool = false
    }
    return magic.toString()
}

fun applySha256(input: String): String {
    return try {
        val digest = MessageDigest.getInstance("SHA-256")
        /* Applies sha256 to our input */
        val hash = digest.digest(input.toByteArray(charset("UTF-8")))
        val hexString = StringBuilder()
        for (elem in hash) {
            val hex = Integer.toHexString(0xff and elem.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        hexString.toString()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}

class Blockchain {
    @Volatile
    var n = 0
    val blockchain = mutableListOf<Block>()
    @Volatile
    var data = "no messages"
    @Volatile
    var message = ""

    @Synchronized fun addBlock(block: Block){
        if (blockchain.isEmpty() || block.hashPrev == blockchain.last().hash) {

            if (block.timeGen < 10 && n < 6) {
                n++
                block.optionN = "N was increased to $n"
            } else if (block.timeGen > 20) {
                n--
                block.optionN = "N was decreased by 1"
            } else block.optionN = "N stays the same"
            data = message
            message = ""
            blockchain += block
        }
    }

    fun generateBlock(): Block {
        return if (blockchain.isEmpty()) {
            run {
                val id = 1
                val time = System.currentTimeMillis()
                val hashPrev = "0"
                Block(n, id, time, hashPrev, Thread.currentThread().name, data)
            }
        } else {
            run {
                val id = blockchain.size + 1
                val time = System.currentTimeMillis()
                val hashPrev = blockchain.last().hash
                Block(n, id, time, hashPrev, Thread.currentThread().name, data)
            }
        }
    }

    private fun controlBlocks(): Boolean {
        var res = true
        for (i in blockchain.lastIndex downTo 1) {
            if (blockchain[i].hashPrev != blockchain[i - 1].hash) res = false
        }
        return res
    }

    fun printBlocks() {
        if (controlBlocks()) {
            println(blockchain.joinToString("\n\n"))
        } else println("ERROR blockchain")
    }
}

class Mainer(private val blockchain: Blockchain) : Thread() {
    override fun run() {
        while (blockchain.blockchain.size < 5) {
            sleep(1000)
            val block = blockchain.generateBlock()
            blockchain.addBlock(block)
        }
    }
}

fun main() {

    val myBlockchain = Blockchain()
    val list = listOf(
        "Tom: Hey, I'm first!",
        "Sarah: It's not fair!",
        "Sarah: You always will be first because it is your blockchain!",
        "Sarah: Anyway, thank you for this amazing chat.",
        "Tom: You're welcome :)",
        "Nick: Hey Tom, nice chat",
        "Hi!!!"
    )

    val mainer1 = Mainer(myBlockchain)
    mainer1.name = "1"
    val mainer2 = Mainer(myBlockchain)
    mainer2.name = "2"
    val mainer3 = Mainer(myBlockchain)
    mainer3.name = "3"
    val mainer4 = Mainer(myBlockchain)
    mainer4.name = "4"
    val mainer5 = Mainer(myBlockchain)
    mainer5.name = "5"

    mainer1.start()
    mainer2.start()
    mainer3.start()
    mainer4.start()
    mainer5.start()

    while (myBlockchain.blockchain.size < 5) {
        myBlockchain.message += "\n${list[Random.nextInt(0, list.lastIndex)]}"
        Thread.sleep(1000)
    }

    myBlockchain.printBlocks()

}