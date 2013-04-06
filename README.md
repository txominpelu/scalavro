# Scalavro

A runtime reflection-based Avro library in Scala.

## Motivation
1. We would rather write Scala classes than Avro IDL.
2. We would like to avoid adding a compilation phase to generate IO bindings.

## Goals
1. To provide an in-memory representation of avro schemas and protocols.
2. To generate avro schemas and protocols automatically from Scala types.
3. To produce flat (.avsc) Avro schema files.
4. To generate Scala bindings for reading and writing Avro-mapped Scala types.
5. Generally, to minimize fuss required to create an Avro-capable Scala application.

## General Information
- Built against Scala 2.10.1 with SBT 0.12.2
- Depends upon [spray-json](https://github.com/spray/spray-json)
- The `io` sub-project depends upon the Apache Java implementation of Avro (Version 1.7.4)

## Usage: Schema Generation

    package com.gensler.scalavro.tests

    import com.gensler.scalavro.types._
    import scala.util.{Try, Success, Failure}

    case class Person(name: String, age: Int)
    val Success(personAvroType) = AvroType.fromType[Person]
    personAvroType.schema

Which yields:

    {
      "name": "Person",
      "type": "record",
      "fields": [
        {"name": "name","type": "string"},
        {"name": "age","type": "int"}
      ],
      "namespace": "com.gensler.scalavro.tests"
    }

And perhaps more interestingly:

    case class SantaList(nice: Seq[Person], naughty: Seq[Person])
    val Success(santaListAvroType) = AvroType.fromType[SantaList]
    santaListAvroType.schema

Which yields:

    {
      "name": "SantaList",
      "type": "record",
      "fields": [
        {
          "name": "nice",
          "type": {"type": "array", "items": "Person"}
        },
        {
          "name": "naughty",
          "type": {"type": "array","items": "Person"}
        }
      ],
      "namespace": "com.gensler.scalavro.tests"
    }

## Usage: Binary IO

    import com.gensler.scalavro.AvroType
    import com.gensler.scalavro.io.AvroTypeIO
    import com.gensler.scalavro.io.AvroTypeIO.Implicits._ // adds IO fxns to AvroType
    import scala.util.{Try, Success, Failure}

    case class Person(name: String, age: Int)
    case class SantaList(nice: Seq[Person], naughty: Seq[Person])

    val santaList = SantaList(
      nice = Seq(
        Person("John", 17),
        Person("Eve", 3)
      ),
      naughty = Seq(
        Person("Jane", 25),
        Person("Alice", 65)
      )
    )

    val santaListType = AvroType.fromType[SantaList].get

    val outStream: java.io.OutputStream = // some stream...
    santaListType.write(santaList, outStream)

    val inStream: java.io.InputStream = // some stream...
    santaListType.read(inStream) match {
      case Success(readResult) => // readResult is an instance of SantaList
      case Failure(cause)      => // handle failure...
    }


## Current Capabilities
- Schema generation for basic types
- Schema generation for sequences (`List`, `Seq`, `Array`, etc)
- Schema generation for String-keyed `Map` types
- Type-safe Avro protocol definitions and JSON output
- Robustness in the face of cyclic type dependencies (such records are never valid Avro)
- Schema conversion to "Parsing Canonical Form"
- Convenient binary IO for primitive types

## Current Limitations
- Binary IO for complex types is incomplete.
- JSON IO is not yet implemented.
- Reading JSON schemas is not yet supported.
- Only binary disjunctive union types are currently supported (via `scala.Either[A, B]`).  We are working on potentially representing these types as unboxed union type definitions.
- Enums are not yet supported.

## Reference
1. [Current Apache Avro Specification](http://avro.apache.org/docs/current/spec.html)
1. [Scala 2.10 Reflection Overview](http://docs.scala-lang.org/overviews/reflection/overview.html)
1. [Great article on schema evolution in various serialization systems](http://martin.kleppmann.com/2012/12/05/schema-evolution-in-avro-protocol-buffers-thrift.html)
1. [Wickedly clever technique for representing unboxed union types, proposed by Miles Sabin](http://chuusai.com/2011/06/09/scala-union-types-curry-howard)

## Legal
Apache Avro is a trademark of The Apache Software Foundation.

Scalavro is distributed under the BSD 2-Clause License, the text of which follows:

Copyright (c) 2013, Gensler  
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

- Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
