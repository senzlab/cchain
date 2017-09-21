package com.score.cchain.util

import java.awt.{Color, Font}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}
import javax.imageio.ImageIO

object ChequeFactory extends App {
  loadCheque()

  def loadCheque() = {
    val src = getClass.getResource("/chq.jpg")
    val img = ImageIO.read(src)

    //val img = ImageIO.read(new File("/Users/eranga/Desktop/chq.jpg"))
    val g2 = img.createGraphics()

    //val im = ImageIO.read(new ByteArrayInputStream("e".getBytes))

    val f = new Font("sans", Font.BOLD, 20)
    g2.setFont(f)
    g2.setColor(Color.BLACK)

    g2.drawString("eranga bandara herath", 40, 50)
    g2.drawString("two thousand five hunderd", 40, 80)

    ImageIO.write(img, "jpg", new File("/Users/eranga/Desktop/chql.jpg"))

    val baos = new ByteArrayOutputStream()
    ImageIO.write(img, "jpg", baos)
    baos.toByteArray
  }
}
