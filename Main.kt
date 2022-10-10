package blockchain

import java.security.MessageDigest

data class Block(val id: Int, val time: Long, val hashPrev: String) {
    var hash = applySha256("$id $time $hashPrev")
    override fun toString(): String {
        return """
            Block:
            Id: $id
            Timestamp: $time
            Hash of the previous block:
            $hashPrev
            Hash of the block:
            $hash
        """.trimIndent()
    }
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

    fun generateBlock() {
        blockchain += if (blockchain.isEmpty()) {
            run {
                val id = 1
                val time = System.currentTimeMillis()
                val hashPrev = "0"
                Block(id, time, hashPrev)
            }
        } else {
            run {
                val id = blockchain.size + 1
                val time = System.currentTimeMillis()
                val hashPrev = blockchain.last().hash
                Block(id, time, hashPrev)
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
    val myBlockchain = Blockchain()
    for (i in 1..5) myBlockchain.generateBlock()
    myBlockchain.printBlocks()
}