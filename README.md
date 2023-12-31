# iopay-web3sdk-kotlin
Web3 SDK is developed based on the [web3j](https://github.com/web3j/web3j) library, 
which simplifies the process of querying balance, transactions and executing contracts.

## Quick Start

### Integration
You can directly import the web3 module in your code and then synchronize it.
```kotlin
   implementation project(':web3')
```

### Create a Web3Manager

```kotlin
    // To get started, you need to create a Web3manager instance that supports querying contracts,
    // reading and writing contract data, and signing data.
    val web3Manger = Web3Manager.build(rpc, chainId, fromAddress)
```

### Code Example

Check wallet balance, support batch query.
```kotlin
    val addressList = listOf("Your wallet address")
    val balanceList = web3Manger.getCurrencyBalance(*addressList.toTypedArray())
```

Check token balance, support batch query.
```kotlin
   val addressList = listOf("Erc20 contract address")
   val balanceList = web3Manger.getErc20Balance(*addressList.toTypedArray())
```

Transfer currency
```kotlin
    val privateKey = "Your private key"
    val to = "Received wallet address"
    val value = Convert.toWei("1", Convert.Unit.ETHER).toBigInteger()
    val gasPrice = DefaultGasProvider.GAS_PRICE
    val gasLimit = DefaultGasProvider.GAS_LIMIT
    val transaction = web3Manger.transferCurrency(to, value, gasPrice, gasLimit, privateKey)
    if (transaction != null) {
        val receipt = web3Manger.queryTransactionReceipt(transaction.transactionHash)
        print(receipt?.isStatusOK)
    }
```

Transfer tokens
```kotlin
    val privateKey = "Your private key"
    val contract = "Erc20 contract address"
    val to = "Received wallet address"
    val value = Convert.toWei("1", Convert.Unit.ETHER).toBigInteger()
    val gasPrice = DefaultGasProvider.GAS_PRICE
    val gasLimit = DefaultGasProvider.GAS_LIMIT
    val transaction = web3Manger.transferErc20(contract, to, value, gasPrice, gasLimit, privateKey)
    if (transaction != null) {
        val receipt = web3Manger.queryTransactionReceipt(transaction.transactionHash)
        print(receipt?.isStatusOK)
    }
```

