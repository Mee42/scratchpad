import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onEach
import java.util.ArrayDeque
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference


@ExperimentalCoroutinesApi
fun main() {
    runBlocking {
        listOf(7, 3, 4, 12, 1, 7, 8).betterSleepSort().onEach {
            println(it)
        }
        delay(10000)
    }


}


@ExperimentalCoroutinesApi
suspend fun List<Int>.betterSleepSort()= coroutineScope {
    channelFlow {
        for(elem in this@betterSleepSort) {
            launch(context = this.coroutineContext) {
                delay(elem * 10L)
                println("sending")
                send(elem)
            }
        }
    }
}

fun List<Int>.sleepSort(): Iterator<Int> {
    val queue = AtomicReference(ArrayDeque<Int>())
    val isDone = AtomicBoolean(false)
    
    GlobalScope.launch {
        val children = mutableListOf<Job>()
        for(elem in this@sleepSort) {
            children += launch {
                delay(elem * 5L)
                queue.updateAndGet { it.addLast(elem); it }
            }
        }
        children.forEach { it.join() }
        isDone.set(true)
        println("Done")
    }

    return object: Iterator<Int> {
        override fun hasNext(): Boolean {
            return queue.get().isNotEmpty() || isDone.get().not()
        }

        override fun next(): Int {
            // block, sorry
            return runBlocking<Int> {
                while(true) {
                    if(queue.get().isEmpty()) continue
                    val x = queue.acquire
                    val v = x.removeLast()
                    queue.setRelease(x)
                    return@runBlocking v
                }
                return@runBlocking 0 // oof
            }
        }

    }
}