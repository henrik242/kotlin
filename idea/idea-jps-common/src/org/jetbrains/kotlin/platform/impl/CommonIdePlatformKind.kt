/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:JvmName("CommonIdePlatformUtil")
package org.jetbrains.kotlin.platform.impl

import org.jetbrains.kotlin.analyzer.common.CommonPlatform
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.K2MetadataCompilerArguments
import org.jetbrains.kotlin.config.TargetPlatformVersion
import org.jetbrains.kotlin.platform.IdePlatform
import org.jetbrains.kotlin.platform.IdePlatformKind

object CommonIdePlatformKind : IdePlatformKind<CommonIdePlatformKind>() {

    override fun platformByCompilerArguments(arguments: CommonCompilerArguments): IdePlatform<CommonIdePlatformKind, CommonCompilerArguments>? {
        return if (arguments is K2MetadataCompilerArguments) Platform
        else null
    }

    override val compilerPlatform get() = CommonPlatform

    override val platforms get() = listOf(Platform)
    override val defaultPlatform get() = Platform

    override val argumentsClass get() = K2MetadataCompilerArguments::class.java

    override val name get() = "Common (experimental)"

    object Platform : IdePlatform<CommonIdePlatformKind, CommonCompilerArguments>() {
        override val kind get() = CommonIdePlatformKind
        override val version get() = TargetPlatformVersion.NoVersion
        override fun createArguments(init: CommonCompilerArguments.() -> Unit) = K2MetadataCompilerArguments().apply(init)
    }
}

val IdePlatformKind<*>?.isCommon
    get() = this is CommonIdePlatformKind

val IdePlatform<*, *>?.isCommon
    get() = this is CommonIdePlatformKind.Platform