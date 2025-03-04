package com.github.zly2006.reden.utils

import com.github.zly2006.reden.Reden
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Heightmap
import net.minecraft.world.World

lateinit var server: MinecraftServer

fun Vec3d.toBlockPos(): BlockPos {
    return BlockPos.ofFloored(this)
}

fun PlayerEntity.sendMessage(s: String) {
    sendMessage(Text.literal(s))
}

fun <E> MutableList<E>.removeAtOrNull(index: Int): E? {
    val i = if (index < 0) size + index else index
    return if (i in indices) removeAt(i) else null
}

fun World.setBlockNoPP(pos: BlockPos, state: BlockState, flags: Int) {
    if (isClient) {

    }
    val stateBefore = getBlockState(pos)
    if (stateBefore.hasBlockEntity()) {
        removeBlockEntity(pos)
    }
    getChunk(pos).run { getSection(getSectionIndex(pos.y)) }
        .setBlockState(pos.x and 15, pos.y and 15, pos.z and 15, state, false)
    getChunk(pos).run {
        this.heightmaps[Heightmap.Type.MOTION_BLOCKING]!!.trackUpdate(pos.x and 15, pos.y, pos.z and 15, state)
        this.heightmaps[Heightmap.Type.MOTION_BLOCKING_NO_LEAVES]!!.trackUpdate(pos.x and 15, pos.y, pos.z and 15, state)
        this.heightmaps[Heightmap.Type.OCEAN_FLOOR]!!.trackUpdate(pos.x and 15, pos.y, pos.z and 15, state)
        this.heightmaps[Heightmap.Type.WORLD_SURFACE]!!.trackUpdate(pos.x and 15, pos.y, pos.z and 15, state)
    }
    if (this is ServerWorld) {
        chunkManager.markForUpdate(pos)
    }
    if (flags and Block.NOTIFY_LISTENERS != 0) {
        updateListeners(pos, getBlockState(pos), state, flags)
    }
}

val isClient: Boolean get() = FabricLoader.getInstance().environmentType == EnvType.CLIENT

object ResourceLoader {
    fun loadBytes(path: String): ByteArray? {
        return Reden::class.java.classLoader.getResourceAsStream(path)?.readAllBytes()
    }

    fun loadString(path: String): String {
        return loadBytes(path)!!.decodeToString()
    }

    fun loadStringOrNull(path: String): String? {
        return loadBytes(path)?.decodeToString()
    }

    fun loadTexture(path: String): Identifier {
        return Identifier("reden", path)
    }

    @JvmStatic
    fun loadLang(lang: String) =
        loadStringOrNull("assets/reden/lang/$lang.json")?.let {
            Json.decodeFromString(MapSerializer(String.serializer(), String.serializer()), it)
        }
}

fun buttonWidget(x: Int, y: Int, width: Int, height: Int, message: Text, onPress: ButtonWidget.PressAction) =
    ButtonWidget(x, y, width, height, message, onPress) { it.get() }

val isSinglePlayerAndCheating: Boolean get() {
    infix fun Boolean?.and(other: Boolean?) = this ?: false && other ?: false
    return MinecraftClient.getInstance()?.let {
        (it.server?.isSingleplayer and it.player?.hasPermissionLevel(2))
    } == true
}

fun memorySizeToString(size: Int) {
    val unit = arrayOf("B", "KB", "MB", "GB", "TB")
    var i = 0
    var s = size.toDouble()
    while (s > 1024) {
        s /= 1024
        i++
    }
    println("%.2f".format(s) + unit[i])
}

fun ULong.toByteArray(): ByteArray =
    arrayOf(
        ((this and 0xff00000000000000uL) shr (7 * 8)).toByte(),
        ((this and 0x00ff000000000000uL) shr (6 * 8)).toByte(),
        ((this and 0x0000ff0000000000uL) shr (5 * 8)).toByte(),
        ((this and 0x000000ff00000000uL) shr (4 * 8)).toByte(),
        ((this and 0x00000000ff000000uL) shr (3 * 8)).toByte(),
        ((this and 0x0000000000ff0000uL) shr (2 * 8)).toByte(),
        ((this and 0x000000000000ff00uL) shr (1 * 8)).toByte(),
        ((this and 0x00000000000000ffuL) shr (0 * 8)).toByte(),
    ).toByteArray()

fun Long.toByteArray(): ByteArray = this.toULong().toByteArray()

fun ulongFromByteArray(data: ByteArray): ULong? {
    if (data.size < 8) {
        return null
    }
    return ((data[0].toULong() shl (7 * 8)) +
            (data[1].toULong() shl (6 * 8)) +
            (data[2].toULong() shl (5 * 8)) +
            (data[3].toULong() shl (4 * 8)) +
            (data[4].toULong() shl (3 * 8)) +
            (data[5].toULong() shl (2 * 8)) +
            (data[6].toULong() shl (1 * 8)) +
            (data[7].toULong() shl (0 * 8)))
}

fun longFromByteArray(data: ByteArray): Long? = ulongFromByteArray(data)?.toLong()

