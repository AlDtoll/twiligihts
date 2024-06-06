package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.Stock

interface IStocks {

    fun value(): ArrayList<Stock>?
    fun update(list: ArrayList<Stock>)
}