package com.maantrack.service

import org.mindrot.jbcrypt.BCrypt

object Crypto {

  def encrypt(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())

  def checkPassword(password: String, encryptPassword: String): Boolean = BCrypt.checkpw(password, encryptPassword)
}
