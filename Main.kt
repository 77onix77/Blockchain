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
Created by: $nameThread
$nameThread gets 100 VC
Id: $id
Timestamp: $time
Magic number: $magic
Hash of the previous block:
$hashPrev
Hash of the block:
$hash
Block data:
${data.trim()}
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
    var data = "No transactions"
    @Volatile
    var message = ""
    private val account = mutableMapOf(
        "miner1" to 0,
        "miner2" to 0,
        "miner3" to 0,
        "miner4" to 0,
        "miner5" to 0,
        "miner6" to 0,
        "miner7" to 0
    )
    private val list = listOf(
        "miner1",
        "miner2",
        "miner3",
        "miner4",
        "miner5",
        "miner6",
        "miner7"
    )

    fun transaction() {
        val payer = list[Random.nextInt(0, 7)]
        val recipient= list[Random.nextInt(0, 7)]
        if (payer != recipient && account[payer]!! > 1) {
            val sum = Random.nextInt(1, account[payer]!!)
            account[payer] = account[payer]!! - sum
            account[recipient] = account[recipient]!! + sum
            message += "$payer sent $sum VC to $recipient\n"
        }
    }

    @Synchronized fun addBlock(block: Block){
        if (blockchain.isEmpty() || block.hashPrev == blockchain.last().hash) {

            if (block.timeGen < 5 && n < 4) {
                n++
                block.optionN = "N was increased to $n"
            } else if (block.timeGen > 15) {
                n--
                block.optionN = "N was decreased by 1"
            } else block.optionN = "N stays the same"
            data = message.ifEmpty { "No transactions" }
            message = ""
            account[Thread.currentThread().name] = account[Thread.currentThread().name]!! + 100
            blockchain += block
            println(block)
            println()
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




}

class Miner(private val blockchain: Blockchain) : Thread() {
    override fun run() {
        while (blockchain.blockchain.size < 15) {
            sleep(500)
            val block = blockchain.generateBlock()
            blockchain.addBlock(block)
        }
    }
}

fun main() {
    val myBlockchain = Blockchain()

    val miner1 = Miner(myBlockchain)
    miner1.name = "miner1"
    val miner2 = Miner(myBlockchain)
    miner2.name = "miner2"
    val miner3 = Miner(myBlockchain)
    miner3.name = "miner3"
    val miner4 = Miner(myBlockchain)
    miner4.name = "miner4"
    val miner5 = Miner(myBlockchain)
    miner5.name = "miner5"
    val miner6 = Miner(myBlockchain)
    miner6.name = "miner6"
    val miner7 = Miner(myBlockchain)
    miner7.name = "miner7"

    miner1.start()
    miner2.start()
    miner3.start()
    miner4.start()
    miner5.start()
    miner6.start()
    miner7.start()

    while (myBlockchain.blockchain.size < 15) {
        myBlockchain.transaction()
        Thread.sleep(200)
    }
}