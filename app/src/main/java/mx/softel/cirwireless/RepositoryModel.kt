package mx.softel.cirwireless

class RepositoryModel (val idClient: Int) {
    private val NA = "NO AVAILABLE"

    private var repositoryUrl  : String
    private var port           : String
    private var path           : String
    private var imagePrefix    : String
    private var imageVersion   : String

    init {
        repositoryUrl   = decideRepository()
        port            = decidePort()
        path            = decidePath()
        imagePrefix     = decideImagePrefix()
        imageVersion    = decideImageVersion()
    }

    // TODO("POR EL MOMENTO SE HA DEJADO UN ID CLIENTE DUMMY")
    private fun decideRepository () : String {
        return when (idClient) {
            1 -> "repository.otus.com.mx"
            else -> NA
        }
    }


    private fun decidePort () : String {
        return when (idClient) {
            1 -> "443"
            else -> NA
        }
    }

    private fun decidePath () : String {
        return when (idClient) {
            1 -> "/esp/images"
            else -> NA
        }
    }

    private fun decideImagePrefix () : String {
        return when (idClient) {
            1 -> "esp-cirwless"
            else -> NA
        }
    }

    private fun decideImageVersion () : String {
        return when (idClient) {
            1 -> "1,\"0.2.0-3\""
            else -> NA
        }
    }

    fun isAValidRepositoryUrl () : Boolean = repositoryUrl == NA || port == NA || path == NA || imagePrefix == NA

    // Getters --------------------------
    fun repositoryUrl () : String = repositoryUrl

    fun port () : String = port

    fun path () : String = path

    fun imagePrefix () : String = imagePrefix

    fun imageVersion () : String = imageVersion
    // ----------------------------------

    // Setters --------------------------
    // ----------------------------------
}