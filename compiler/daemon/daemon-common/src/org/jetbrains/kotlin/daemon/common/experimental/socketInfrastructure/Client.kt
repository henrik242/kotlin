package org.jetbrains.kotlin.daemon.common.experimental.socketInfrastructure

import io.ktor.network.sockets.Socket
import kotlinx.coroutines.experimental.*
import org.jetbrains.kotlin.daemon.common.experimental.LoopbackNetworkInterface
import sun.net.ConnectionResetException
import java.beans.Transient
import java.io.Serializable
import java.util.logging.Logger

interface Client : Serializable, AutoCloseable {
    @Throws(Exception::class)
    fun connectToServer()

    fun sendMessage(msg: Any): Deferred<Unit>
    fun <T> readMessage(): Deferred<T>

}

@Suppress("UNCHECKED_CAST")
class DefaultClient(
    val serverPort: Int,
    val serverHost: String = LoopbackNetworkInterface.loopbackInetAddressName,
    val authorizeOnServer: (ByteWriteChannelWrapper) -> Unit = {}
) : Client {

    val log: Logger
        @Transient get() = Logger.getLogger("default client")

    lateinit var input: ByteReadChannelWrapper
        @Transient get
        @Transient set

    lateinit var output: ByteWriteChannelWrapper
        @Transient get
        @Transient set

    private var socket: Socket? = null
        @Transient get
        @Transient set
    override fun close() {
        socket?.close()
    }

    override fun sendMessage(msg: Any) = async { output.writeObject(msg) }

    override fun <T> readMessage() = async { input.nextObject() as T }

    @Throws(Exception::class)
    override fun connectToServer() {
        runBlocking(Unconfined) {
            log.info("connectToServer (port = $serverPort | host = $serverHost)")
            try {
                socket = LoopbackNetworkInterface.clientLoopbackSocketFactoryKtor.createSocket(
                    serverHost,
                    serverPort
                )
            } catch (e: Throwable) {
                log.info("EXCEPTION while connecting to server ($e)")
                throw e
            }
            log.info("connected (port = $serverPort, serv =$serverPort)")
            socket!!.openIO(log).also {
                log.info("OK serv.openIO() |port=$serverPort|")
                input = it.input
                output = it.output
                if (!tryAcquireHandshakeMessage(input, log) || !trySendHandshakeMessage(output)) {
                    throw ConnectionResetException("failed to establish connection with server (handshake failed)")
                }
                authorizeOnServer(output)
            }
        }
    }

}

class DefaultClientRMIWrapper : Client {
    override fun connectToServer() {}
    override fun sendMessage(msg: Any) = throw UnsupportedOperationException("sendMessage is not supported for RMI wrappers")
    override fun <T> readMessage() = throw UnsupportedOperationException("readMessage is not supported for RMI wrappers")
    override fun close() {}
}