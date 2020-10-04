package pl.tomaszstankowski.fourplayerchess.engine

internal interface Search {

    fun startSearch(maxDepth: Int): SearchTask

    fun stopSearch()
}