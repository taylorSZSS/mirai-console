/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.console.setting

import kotlinx.serialization.KSerializer
import net.mamoe.mirai.console.setting.internal.cast
import net.mamoe.mirai.console.setting.internal.valueFromKTypeImpl
import net.mamoe.mirai.console.setting.internal.valueImpl
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import java.util.*
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf


// TODO: 2020/6/21 move to JvmPlugin to inherit SettingStorage and CoroutineScope for saving
// Shows public APIs such as deciding when to auto-save.
abstract class Setting : SettingImpl() {

    operator fun <T> SerializerAwareValue<T>.provideDelegate(
        thisRef: Any?,
        property: KProperty<*>
    ): SerializerAwareValue<T> {
        @Suppress("UNCHECKED_CAST")
        valueNodes.add(Node(property as KProperty<T>, this, this.serializer))
        return this
    }
}

/**
 * Internal implementation for [Setting] including:
 * - Reflection on Kotlin properties and Java fields
 * - Auto-saving
 */
// TODO move to internal package.
internal abstract class SettingImpl {
    internal class Node<T>(
        val property: KProperty<T>,
        val value: Value<T>,
        val updaterSerializer: KSerializer<Unit>
    )

    internal val valueNodes: MutableList<Node<*>> = Collections.synchronizedList(mutableListOf())

    /**
     * flatten
     */
    internal fun onValueChanged(value: Value<*>) {
        // TODO: 2020/6/22
    }
}


//// region Setting.value primitives CODEGEN ////

// TODO: 2020/6/19 CODEGEN

fun Setting.value(default: Int): SerializableValue<Int> = valueImpl(default)

//// endregion Setting.value primitives CODEGEN ////


/**
 * Creates a [Value] with reified type.
 *
 * @param T reified param type T.
 * Supports only primitives, Kotlin built-in collections,
 * and classes that are serializable with Kotlinx.serialization
 * (typically annotated with [kotlinx.serialization.Serializable])
 */
@LowPriorityInOverloadResolution
@OptIn(ExperimentalStdlibApi::class) // stable in 1.4
inline fun <reified T> Setting.valueReified(default: T): SerializableValue<T> = valueFromKTypeImpl(typeOf<T>()).cast()

@MiraiExperimentalAPI
fun <T> Setting.valueFromKType(type: KType): SerializableValue<T> = valueFromKTypeImpl(type).cast()