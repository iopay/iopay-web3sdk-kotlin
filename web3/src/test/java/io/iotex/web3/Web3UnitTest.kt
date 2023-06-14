package io.iotex.web3

import org.junit.Test
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Convert
import java.math.BigInteger

class Web3UnitTest {

    private val web3Manger by lazy {
        Web3Manger.build("", 4689, "")
    }

    @Test
    fun testGetCurrencyBalance() {
        val addressList = listOf("0x2ee1d96cb76579e2c64c9bb045443fb3849491d2")
        val result = web3Manger.getCurrencyBalance(*addressList.toTypedArray())
        val balance = if (result.isNotEmpty()) result[0] else BigInteger.ZERO
        val str = Convert.fromWei(balance.toString(), Convert.Unit.ETHER).toPlainString()
        print(str)
    }

    @Test
    fun testGetErc20Balance() {
        val addressList = listOf("0xd77f23c1de0adeda398a521a40df841159d851b6")
        val result = web3Manger.getErc20Balance(*addressList.toTypedArray())
        val balance = if (result.isNotEmpty()) result[0] else BigInteger.ZERO
        val str = Convert.fromWei(balance.toString(), Convert.Unit.ETHER).toPlainString()
        print(str)
    }

    @Test
    fun testTransferCurrency() {
        val to = "0x2ee1d96cb76579e2c64c9bb045443fb3849491d2"
        val value = Convert.toWei("1", Convert.Unit.ETHER).toBigInteger()
        val gasPrice = DefaultGasProvider.GAS_PRICE
        val gasLimit = DefaultGasProvider.GAS_LIMIT
        val transaction = web3Manger.transferCurrency(to, value, gasPrice, gasLimit)
        if (transaction != null) {
            val receipt = web3Manger.queryTransactionReceipt(transaction.transactionHash)
            print(receipt?.isStatusOK)
        }
    }

    @Test
    fun testTransferErc20() {
        val contract = "0xd77f23c1de0adeda398a521a40df841159d851b6"
        val to = "0x2ee1d96cb76579e2c64c9bb045443fb3849491d2"
        val value = Convert.toWei("1", Convert.Unit.ETHER).toBigInteger()
        val gasPrice = DefaultGasProvider.GAS_PRICE
        val gasLimit = DefaultGasProvider.GAS_LIMIT
        val transaction = web3Manger.transferErc20(contract, to, value, gasPrice, gasLimit)
        if (transaction != null) {
            val receipt = web3Manger.queryTransactionReceipt(transaction.transactionHash)
            print(receipt?.isStatusOK)
        }
    }

    @Test
    fun testSignMessage() {
        val message = "Your signature message"
        val result = web3Manger.signMessage(message.toByteArray(), true)
        print(result)
    }

    @Test
    fun testIsContract() {
        val contract = "0xd77f23c1de0adeda398a521a40df841159d851b6"
        val result = web3Manger.isContract(contract)
        print(result)
    }

    @Test
    fun testErc20Name() {
        val erc20 = "0xd77f23c1de0adeda398a521a40df841159d851b6"
        val name = web3Manger.erc20Name(erc20)
        print(name)
    }

    @Test
    fun testErc20Symbol() {
        val erc20 = "0xd77f23c1de0adeda398a521a40df841159d851b6"
        val symbol = web3Manger.erc20Symbol(erc20)
        print(symbol)
    }

    @Test
    fun testErc20Decimals() {
        val erc20 = "0xd77f23c1de0adeda398a521a40df841159d851b6"
        val decimals = web3Manger.erc20Decimals(erc20)
        print(decimals)
    }



}