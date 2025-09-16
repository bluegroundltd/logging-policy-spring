import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponseWrapper
import java.io.IOException
import java.io.PrintWriter
import java.io.Writer
import java.nio.charset.Charset


/**
 * A [HttpServletResponseWrapper] that counts the number of bytes written to the response.
 *
 * NOTE:
 * We use this instead of ContentCachingResponseWrapper as the latter
 * stores the entire response content in memory!
 */
class CountingHttpServletResponseWrapper(response: HttpServletResponse) : HttpServletResponseWrapper(response) {

  private var byteCount: Int = 0
  private var countingOutputStream: ServletOutputStream? = null
  private var countingWriter: PrintWriter? = null

  /**
   * Returns the total number of bytes written to the response.
   */
  fun getByteCount(): Int {
    return byteCount
  }

  @Throws(IOException::class)
  override fun getOutputStream(): ServletOutputStream {
    if (countingWriter != null) {
      throw IllegalStateException("getWriter() has already been called on this response.")
    }
    if (countingOutputStream == null) {
      val originalOutputStream = super.getOutputStream()
      countingOutputStream = CountingServletOutputStream(originalOutputStream)
    }
    return countingOutputStream!!
  }

  @Throws(IOException::class)
  override fun getWriter(): PrintWriter {
    if (countingOutputStream != null) {
      throw IllegalStateException("getOutputStream() has already been called on this response.")
    }
    if (countingWriter == null) {
      val originalWriter = super.getWriter()
      countingWriter = PrintWriter(CountingWriter(originalWriter))
    }
    return countingWriter!!
  }

  private inner class CountingServletOutputStream(
    private val outputStream: ServletOutputStream
  ) : ServletOutputStream() {

    @Throws(IOException::class)
    override fun write(b: Int) {
      outputStream.write(b)
      byteCount++
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray) {
      outputStream.write(b)
      byteCount += b.size
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray, off: Int, len: Int) {
      outputStream.write(b, off, len)
      byteCount += len
    }

    override fun isReady(): Boolean {
      return outputStream.isReady
    }

    override fun setWriteListener(writeListener: WriteListener?) {
      outputStream.setWriteListener(writeListener)
    }
  }

  private inner class CountingWriter(
    private val writer: Writer
  ) : Writer() {

    @Throws(IOException::class)
    override fun write(cbuf: CharArray, off: Int, len: Int) {
      writer.write(cbuf, off, len)
      val charSequence = String(cbuf, off, len)
      val encoding = response.characterEncoding ?: "UTF-8"
      val bytes = charSequence.toByteArray(Charset.forName(encoding))
      byteCount += bytes.size
    }

    @Throws(IOException::class)
    override fun write(c: Int) {
      writer.write(c)
      val charSequence = c.toChar().toString()
      val encoding = response.characterEncoding ?: "UTF-8"
      val bytes = charSequence.toByteArray(Charset.forName(encoding))
      byteCount += bytes.size
    }

    @Throws(IOException::class)
    override fun write(str: String) {
      writer.write(str)
      val encoding = response.characterEncoding ?: "UTF-8"
      val bytes = str.toByteArray(Charset.forName(encoding))
      byteCount += bytes.size
    }

    @Throws(IOException::class)
    override fun write(str: String, off: Int, len: Int) {
      writer.write(str, off, len)
      val substring = str.substring(off, off + len)
      val encoding = response.characterEncoding ?: "UTF-8"
      val bytes = substring.toByteArray(Charset.forName(encoding))
      byteCount += bytes.size
    }

    @Throws(IOException::class)
    override fun flush() {
      writer.flush()
    }

    @Throws(IOException::class)
    override fun close() {
      writer.close()
    }
  }
}
