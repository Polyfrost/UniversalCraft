package org.polyfrost.universal

import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit

//#if STANDALONE
//$$ import org.polyfrost.universal.standalone.glfw.Glfw
//$$ import org.polyfrost.universal.standalone.glfw.GlfwWindow
//$$ import kotlinx.coroutines.Dispatchers
//$$ import kotlinx.coroutines.runBlocking
//$$ import kotlinx.coroutines.withContext
//$$ import org.lwjgl.glfw.GLFW
//$$ import org.lwjgl.glfw.GLFWErrorCallback
//$$ import org.lwjgl.system.MemoryUtil
//#elseif MC>=11400
//#else
import net.minecraft.client.gui.GuiScreen
//#endif

object UDesktop {
    @JvmStatic
    var isLinux: Boolean = false
        private set

    @JvmStatic
    var isXdg: Boolean = false
        private set

    @JvmStatic
    var isKde: Boolean = false
        private set

    @JvmStatic
    var isGnome: Boolean = false
        private set

    @JvmStatic
    var isMac: Boolean = false
        private set

    @JvmStatic
    var isWindows: Boolean = false
        private set

    init {
        val osName = try {
            System.getProperty("os.name")
        } catch (e: SecurityException) {
            null
        }
        isLinux = osName != null && (osName.startsWith("Linux") || osName.startsWith("LINUX"))
        isMac = osName != null && osName.startsWith("Mac")
        isWindows = osName != null && osName.startsWith("Windows")
        if (isLinux) {
            System.getenv("XDG_SESSION_ID")?.let {
                isXdg = it.isNotEmpty()
            }
            System.getenv("GDMSESSION")?.lowercase()?.let {
                isGnome = "gnome" in it
                isKde = "kde" in it
            }
        } else {
            isXdg = false
            isKde = false
            isGnome = false
        }
    }

    @JvmStatic
    fun browse(uri: URI): Boolean = browseDesktop(uri) || openSystemSpecific(uri.toString())

    @JvmStatic
    fun open(file: File): Boolean = openDesktop(file) || openSystemSpecific(file.path)

    @JvmStatic
    fun edit(file: File): Boolean = editDesktop(file) || openSystemSpecific(file.path)

    private fun openSystemSpecific(file: String): Boolean {
        return when {
            isLinux -> listOf("xdg-open", "kde-open", "gnome-open").any { runCommand(it, file, checkExitStatus = true) }
            isMac -> runCommand("open", file)
            isWindows -> runCommand("rundll32", "url.dll,FileProtocolHandler", file)
            else -> false
        }
    }

    private fun browseDesktop(uri: URI): Boolean {
        return if (!Desktop.isDesktopSupported()) false else try {
            if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                return false
            }

            Desktop.getDesktop().browse(uri)
            true
        } catch (e: Throwable) {
            false
        }
    }

    private fun openDesktop(file: File): Boolean {
        return if (!Desktop.isDesktopSupported()) false else try {
            if (!Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
                return false
            Desktop.getDesktop().open(file)
            true
        } catch (e: Throwable) {
            false
        }
    }

    private fun editDesktop(file: File): Boolean {
        return if (!Desktop.isDesktopSupported()) false else try {
            if (!Desktop.getDesktop().isSupported(Desktop.Action.EDIT))
                return false
            Desktop.getDesktop().edit(file)
            true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Runs the given command with arguments via [Runtime.exec].
     *
     * If [checkExitStatus] is true, the method will wait for the process to exit (but at most a few seconds) and then
     * return `false` if the process exit code is non-zero (`true` if the process did not exit in time).
     */
    private fun runCommand(vararg command: String, checkExitStatus: Boolean = false): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(command) ?: return false
            if (checkExitStatus) {
                if (process.waitFor(3, TimeUnit.SECONDS)) {
                    process.exitValue() == 0
                } else {
                    true // still running, assume success
                }
            } else {
                process.isAlive
            }
        } catch (e: IOException) {
            false
        }
    }

    //#if STANDALONE
    //$$ internal lateinit var glfwWindow: GlfwWindow
    //$$
    //$$ @JvmStatic
    //$$ fun getClipboardString(): String = runBlocking {
    //$$     withContext(Dispatchers.Glfw.immediate) {
    //$$         var oldCallback: GLFWErrorCallback? = null
    //$$         oldCallback = GLFW.glfwSetErrorCallback { error, description ->
    //$$             if (error == GLFW.GLFW_FORMAT_UNAVAILABLE) return@glfwSetErrorCallback
    //$$             oldCallback?.invoke(error, description)
    //$$         }
    //$$         try {
    //$$             GLFW.glfwGetClipboardString(glfwWindow.glfwId) ?: ""
    //$$         } finally {
    //$$             GLFW.glfwSetErrorCallback(oldCallback)?.free()
    //$$         }
    //$$     }
    //$$ }
    //$$
    //$$ @JvmStatic
    //$$ fun setClipboardString(str: String) = runBlocking {
    //$$     withContext(Dispatchers.Glfw.immediate) {
    //$$         // If we pass a string, LWJGL allocates a temporary buffer for it on the stack, which is small by default (64kb).
    //$$         // So for large strings, we need to explicitly allocate it on the heap.
    //$$         if (str.length < 512) {
    //$$             GLFW.glfwSetClipboardString(glfwWindow.glfwId, str)
    //$$         } else {
    //$$             val buffer = MemoryUtil.memUTF8(str)
    //$$             try {
    //$$                 GLFW.glfwSetClipboardString(glfwWindow.glfwId, buffer)
    //$$             } finally {
    //$$                 MemoryUtil.memFree(buffer)
    //$$             }
    //$$         }
    //$$     }
    //$$ }
    //#else
    @JvmStatic
    fun getClipboardString(): String =
    //#if MC>=11400
    //$$ UMinecraft.getMinecraft().keyboardListener.clipboardString
        //#else
        GuiScreen.getClipboardString()
    //#endif

    @JvmStatic
    fun setClipboardString(str: String) {
        //#if MC>=11400
        //$$ UMinecraft.getMinecraft().keyboardListener.clipboardString = str
        //#else
        GuiScreen.setClipboardString(str)
        //#endif
    }
    //#endif
}
