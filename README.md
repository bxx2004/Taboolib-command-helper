# Taboolib-command-helper
### 一个简单的命令帮助模板,较原版相比更加美观,仅此而已。

<img width="431" alt="d894fffaf7370f7d1f783a0023755159" src="https://github.com/bxx2004/Taboolib-command-helper/assets/102979712/b076ed26-c3e7-4b5c-8633-8e76b171f55f">
<img width="463" alt="0879441fc8fe35653df0b1fb80afcd7a" src="https://github.com/bxx2004/Taboolib-command-helper/assets/102979712/a7aff8ea-db2d-4519-b108-6cc6738fa9e7">

``` kotlin
newCommand("aciton", checkPermission = true, lang = "zh_cn"){
            literalWithHelper("tell"){
                description("给玩家发送信息")
                dynamicWithHelper("[玩家]"){
                    description("给某玩家发送信息")
                    execute<ProxyCommandSender>{sender, context, argument ->
                        sender.sendMessage(argument)
                    }
                    dynamic("信息内容") {
                        execute<ProxyCommandSender>{sender, context, argument ->
                            var player= Bukkit.getPlayerExact(context.args()[index-1])!!
                            player.sendMessage(argument)
                        }
                    }
                }
            }
        }
```
