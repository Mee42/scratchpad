

@DslMarker
annotation class DSL

@DSL
fun message(block: MessageCreateSpec.() -> Unit): MessageCreateSpec {
    return MessageCreateSpec().apply(block)
}

class MessageCreateSpec {
    var content: String = ""
    var embedSpec: EmbedSpec? = null
    @DSL
    fun embed(block: EmbedSpec.() -> Unit) {
        embedSpec = EmbedSpec().apply(block)
    }
}

class EmbedSpec {
    var title: String = ""
    var desc: String = ""
    val fields = mutableListOf<FieldSpec>()
    @DSL
    fun field(block: FieldSpec.() -> Unit) {
        fields.add(FieldSpec().apply(block))
    }
}
class FieldSpec {
    var name: String? = null
    var desc: String? = null
    var inline: Boolean = true
}



fun test(): MessageCreateSpec {
    return message {
        content = "this is some content"
        embed {
            title = "BIG TITLE"
            desc = "of this example dsl"
            field {
                name = "this is field 1"
                desc = "yay"
                inline = false
            }
            field {
                name = "this is field 2"
                desc = "oof"
                inline = true
            }
        }
    }
}