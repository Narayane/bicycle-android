/**
 * Copyright © 2017 Bicycle (Sébastien BALARD)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sebastienbalard.bicycle

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception

abstract class SBLog {

    private val logger: Logger

    init {
        val clazz = this::class
        if (!clazz.isCompanion)  {
            throw  UnsupportedOperationException("SBLog object must be a companion object")
        }
        val accompanied = Class.forName(clazz.java.name.substringBeforeLast('$'))
        logger = LoggerFactory.getLogger(accompanied)
    }

    fun v(message: String) {
        if (logger.isTraceEnabled) {
            logger.trace(message)
            //crashReport.logMessage("[TRACE]: ", message)
        }
    }

    fun d(message: String) {
        if (logger.isDebugEnabled) {
            logger.debug(message)
            //crashReport.logMessage("[DEBUG]: ", message)
        }
    }

    fun i(message: String) {
        if (logger.isInfoEnabled) {
            logger.info(message)
            //crashReport.logMessage("[INFO]: ", message)
        }
    }

    fun w(message: String) {
        if (logger.isWarnEnabled) {
            logger.warn(message)
            //crashReport.logMessage("[WARN]: ", message)
        }
    }

    fun w(message: String, exception: Exception) {
        if (logger.isWarnEnabled) {
            logger.warn(message, exception)
            /*crashReport.logMessage("[WARN]: ", message)
            exception.cause?.let { throwable ->
                crashReport.catchException(throwable)
            }*/
        }
    }

    fun e(message: String) {
        if (logger.isErrorEnabled) {
            logger.error(message)
            //crashReport.logMessage("[ERROR]: ", message)
        }
    }

    fun e(message: String, exception: Exception) {
        if (logger.isErrorEnabled) {
            logger.error(message, exception)
            /*exception.cause?.let { throwable ->
                crashReport.catchException(throwable)
            }*/
        }
    }

    fun e(message: String, throwable: Throwable) {
        if (logger.isErrorEnabled) {
            logger.error(message, throwable)
            //crashReport.catchException(throwable)
        }
    }
}