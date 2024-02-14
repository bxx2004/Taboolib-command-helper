import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandStructure
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentDynamic
import taboolib.common.platform.command.component.CommandComponentLiteral
import taboolib.module.chat.ComponentText
import taboolib.module.chat.colored
import taboolib.module.chat.component

/**
 * 多语言命令帮助模板
 * @author bxx2004
 * @since 2024/02/14 20:08
 */
private object Language{
    operator fun get(lang:String,node:String):String{
        when(lang){
            "简体中文","chinese","zh_cn"->{
                return when(node){
                    "command"->"命令"
                    "alias"->"别名"
                    "permission"->"权限"
                    "parameter"->"参数"
                    else -> return "&c-$node($lang)-".colored()
                }
            }
            "繁体中文","chinese_tw","zh_tw"->{
                return when(node){
                    "command"->"命令"
                    "alias" -> "别名"
                    "permission" -> "許可權"
                    "parameter"-> "參數"
                    else -> return "&c-$node($lang)-".colored()
                }
            }
            else -> return node
        }
    }
}


private val map = HashMap<CommandComponent,String>()
private var CommandComponent.description:String
    set(value) {
        map[this] = value
    }
    get() = map[this]?:" "
fun CommandComponent.description(value:String){
    description = value
}


fun CommandComponent.createHelper(check:Boolean = true,lang:String = "chinese"):CommandComponent{
    fun name(cmd:CommandComponent):String{
        return when(cmd){
            is CommandComponentLiteral->{
                cmd.aliases[0]
            }
            is CommandComponentDynamic->{
                cmd.comment
            }
            else ->{
                "unknown"
            }
        }
    }
    fun space(space: Int): String {
        return (1..space).joinToString("") { " " }
    }
    fun preLink(cmd:CommandComponent):String{
        var builder = ArrayList<String>()
        if (cmd.parent != null){
            builder.add(preLink(cmd.parent!!))
        }else{
            builder.add(name(cmd))
        }
        return builder.reversed().joinToString(" ")
    }
    fun nextLink(cmd:CommandComponent):String{
        if (cmd.children.isEmpty()){
            return name(cmd)
        }
        var builder = ArrayList<String>()
        builder.add(name(cmd))
        if (cmd.children.size == 1){
            builder.add(nextLink(cmd.children[0]))
        }else{
            builder.add("\\[...\\]")
        }
        return if (builder.joinToString(" ").length>=30){
            builder.joinToString(" ").substring(0,30) + "..."
        }else{
            builder.joinToString(" ")
        }
    }
    fun print(children:List<CommandComponent>,builder:ComponentText,sender: ProxyCommandSender,command:CommandStructure){
        children.forEach {
            when(it){
                is CommandComponentLiteral->{
                    builder.append("    &7- [&7${nextLink(it)}](hover=${it.aliases.filter { index != 0 }.joinToString(",")};cmd=/${if (preLink(it) == "unknown") command.name+" "+it.aliases[0] else preLink(it)+" "+it.aliases[0]})\n".component().buildColored())
                }
                is CommandComponentDynamic->{
                    builder.append("    &7- [&7${nextLink(it)}](hover=${it.comment.replace("(","\\(").replace(")","\\)")};cmd=/${if (preLink(it) == "unknown") command.name else preLink(it)})\n".component().buildColored())
                }
            }
            builder.append("      &f[${it.description}](hover=${it.permission})\n".component().buildColored())
        }
        builder.sendTo(sender)
    }
    execute<ProxyCommandSender> { sender, context, argument ->
        val command = context.command
        val builder = "\n&7&l ${Language[lang,"command"]}: &c&l/${command.name} &f\\[ ${Language[lang,"alias"]}: &b&L${command.aliases.joinToString()}&f \\] &f\\[ ${Language[lang,"permission"]}: &b&L${command.permission}&f \\]\n".component().buildColored()
        builder.append(space(4)+"&f&l${Language[lang,"parameter"]}:\n".colored())
        fun check(children: List<CommandComponent>): List<CommandComponent> {
            // 检查权限
            val filterChildren = if (check) {
                children.filter { sender.hasPermission(it.permission) }
            } else {
                children
            }
            // 过滤隐藏
            return filterChildren.filter { it !is CommandComponentLiteral || !it.hidden }
        }
        print(check(children),builder,sender,command)
    }
    return this
}

fun CommandComponent.literalWithHelper(
    vararg aliases: String,
    optional: Boolean = false,
    permission: String = "",
    hidden: Boolean = false,
    checkPermission:Boolean = true,
    lang:String = "zh_cn",
    literal: CommandComponentLiteral.() -> Unit = {},
): CommandComponentLiteral {
    return literal(aliases = aliases, optional = optional, permission = permission, hidden = hidden, literal = literal).createHelper(checkPermission,lang) as CommandComponentLiteral
}
fun CommandComponent.dynamicWithHelper(
    comment: String = "...",
    optional: Boolean = false,
    permission: String = "",
    checkPermission:Boolean = true,
    lang:String = "zh_cn",
    dynamic: CommandComponentDynamic.() -> Unit = {},
): CommandComponentDynamic {
    return dynamic(comment, optional, permission, dynamic).createHelper(checkPermission,lang) as CommandComponentDynamic
}