package blockchain

import java.security.MessageDigest
import kotlin.random.Random

data class Block(val n: Int, val id: Int, val time: Long, val hashPrev: String) {
    private val magic = magic("$id $time $hashPrev", n)
    val hash = applySha256("$id $time $hashPrev $magic")
    private val timeGen = (System.currentTimeMillis() - time) / 1000
    override fun toString(): String {
        return """
            Block:
            Id: $id
            Timestamp: $time
            Magic number: $magic
            Hash of the previous block:
            $hashPrev
            Hash of the block:
            $hash
            Block was generating for $timeGen seconds
        """.trimIndent()
    }
}

fun magic(str: String, n: Int): String {
    var hash = ""
    var magic: Int = 0
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
    private val blockchain = mutableListOf<Block>()

    fun generateBlock(n: Int) {
        blockchain += if (blockchain.isEmpty()) {
            run {
                val id = 1
                val time = System.currentTimeMillis()
                val hashPrev = "0"
                Block(n, id, time, hashPrev)
            }
        } else {
            run {
                val id = blockchain.size + 1
                val time = System.currentTimeMillis()
                val hashPrev = blockchain.last().hash
                Block(n, id, time, hashPrev)
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

fun main() {
    print("Enter how many zeros the hash must start with: ")
    val n = readln().toInt()
    println()
    val myBlockchain = Blockchain()
    for (i in 1..5) myBlockchain.generateBlock(n)
    myBlockchain.printBlocks()
}