import VerifyResult.Success

sealed class VerifyResult {
    object Success: VerifyResult() {
        override fun map(f: () -> VerifyResult) = f()
    }
    class Fail(val message: (String) -> String): VerifyResult() {
        override fun map(f: () -> VerifyResult)= this
    }

    abstract fun map(f: () -> VerifyResult) :VerifyResult
}
private fun e(message: (String) -> String): VerifyResult.Fail {
    return VerifyResult.Fail(message)
}

interface Verifier {
    fun verify(input: String): VerifyResult
    fun and(other: Verifier): Verifier = DualVerifier(this, other)
}

class DualVerifier(private val a: Verifier, private val b: Verifier): Verifier {
    override fun verify(input: String): VerifyResult {
        return a.verify(input).map { b.verify(input) }
    }
}

object PassVerifier: Verifier {
    override fun verify(input: String): VerifyResult {
        return Success
    }
}
class AllInSet(private val set: Set<Char>, private val setName: String): Verifier {
    override fun verify(input: String): VerifyResult {
        return if(input.all { it in set }) Success else e{"$it must be all $setName" }
    }
    companion object {
        val lower = "abcdefghijklmnopqrstuvwxyz".toCharArray().toSet()
        val upper = lower.map { it.toUpperCase() }.toSet()
        val numbers = ('0'..'9').toSet()
        val characters = "&[{}(=*)+]!#/@\\-%`?^|_\"'".toSet()
        val typeable = lower + upper + numbers + characters
    }
}

object NotBlank: Verifier {
    override fun verify(input: String): VerifyResult {
        return if(input.isBlank()) e{"$it must not be blank"} else Success
    }
}
class LengthLimit(val lower: Int, val upper: Int): Verifier {
    override fun verify(input: String): VerifyResult {
        return if(input.length in (lower..upper)) Success else e{"$it must be between $lower and $upper characters"}
    }
}




fun verifyUsername(username: String): VerifyResult {
    return PassVerifier
        .and(NotBlank)
        .and(LengthLimit(3,  15))
        .and(AllInSet(AllInSet.lower + AllInSet.upper + AllInSet.numbers + "_".toSet(), "letters, numbers, or underscores"))
        .verify(username)
}

fun verifyPassword(password: String): VerifyResult {
    return PassVerifier
        .and(NotBlank)
        .and(LengthLimit(10, 255))
        .and(AllInSet(AllInSet.typeable, "typeable characters"))
        .verify(password)
}


fun main() {
    fun uAssertError(string: String,error: String) {
        when(val result = verifyUsername(string)) {
            Success -> error("expecting \"$error\", got success")
            is VerifyResult.Fail -> {
                if(result.message("username") != error) error("expecting \n> $error\ngot:\n> " + result.message("username"))
            }
        }
    }
    fun uAssert(username: String) {
        val result =  verifyUsername(username)
        if(result is VerifyResult.Fail) error("got error ${result.message("username")}")
    }


    uAssertError("", "username must not be blank")
    uAssertError("a", "username must be between 3 and 15 characters")
    uAssertError("hello-my-name-is-arson", "username must be between 3 and 15 characters")
    uAssertError("spaces lol", "username must be all letters, numbers, or underscores")
    uAssert("under_scores0")


}








