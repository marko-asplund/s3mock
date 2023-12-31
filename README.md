# S3 mock library for Java/Scala

![Build Status](https://github.com/marko-asplund/s3mock/actions/workflows/ci.yml/badge.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.marko-asplund/s3mock_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.marko-asplund/s3mock_2.13)

s3mock is a web service implementing AWS S3 API, which can be used for local testing of your code using S3
but without hitting real S3 endpoints.

Implemented API methods:
* list buckets
* list objects (all & by prefix)
* create bucket
* delete bucket
* put object (via PUT, POST, multipart and chunked uploads are also supported)
* copy object
* get object
* delete object
* batch delete

Not supported features (these might be implemented later):
* authentication: s3proxy will accept any credentials without validity and signature checking
* bucket policy, ACL, versioning
* object ACL
* posix-incompatible key structure with file-based provider, for example keys `/some.dir/file.txt` and `/some.dir` in the same bucket

## Installation

s3mock package is available for Scala 2.12/2.13 (on Java 8/11). To install using SBT, add these
 statements to your `build.sbt`:

    libraryDependencies += "io.github.marko-asplund" %% "s3mock" % "0.5.0" % "test",

On maven, update your `pom.xml` in the following way:
```xml
    // add this entry to <dependencies/>
    <dependency>
        <groupId>io.github.marko-asplund</groupId>
        <artifactId>s3mock_2.13</artifactId>
        <version>0.5.0</version>
        <scope>test</scope>
    </dependency>
```

S3Mock can be run as a standalone application from sbt:
```bash
runMain io.findify.s3mock.Main
```

S3Mock is also available as a [docker container](https://hub.docker.com/r/findify/s3mock/) for out-of-jvm testing:
```bash
docker run -p 8001:8001 findify/s3mock:latest
```

To mount a directory containing the prepared content, mount the volume and set the `S3MOCK_DATA_DIR` environment variable:
```bash
docker run -p 8001:8001 -v /host/path/to/s3mock/:/tmp/s3mock/ -e "S3MOCK_DATA_DIR=/tmp/s3mock" findify/s3mock:latest
```

## Usage

Just point your s3 client to a localhost, enable path-style access, and it should work out of the box.

There are two working modes for s3mock:
* File-based: it will map a local directory as a collection of s3 buckets. This mode can be useful when you need to have a bucket with some pre-loaded data (and too lazy to re-upload everything on each run).
* In-memory: keep everything in RAM. All the data you've uploaded to s3mock will be wiped completely on shutdown. 

Java:
```java
    import com.amazonaws.auth.AWSStaticCredentialsProvider;
    import com.amazonaws.auth.AnonymousAWSCredentials;
    import com.amazonaws.client.builder.AwsClientBuilder;
    import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
    import com.amazonaws.services.s3.AmazonS3;
    import com.amazonaws.services.s3.AmazonS3Builder;
    import com.amazonaws.services.s3.AmazonS3Client;
    import com.amazonaws.services.s3.AmazonS3ClientBuilder;
    import io.findify.s3mock.S3Mock;
    
    /*
     S3Mock.create(8001, "/tmp/s3");
     */
    S3Mock api = new S3Mock.Builder().withPort(8001).withInMemoryBackend().build();
    api.start();
            
    /* AWS S3 client setup.
     *  withPathStyleAccessEnabled(true) trick is required to overcome S3 default 
     *  DNS-based bucket access scheme
     *  resulting in attempts to connect to addresses like "bucketname.localhost"
     *  which requires specific DNS setup.
     */
    EndpointConfiguration endpoint = new EndpointConfiguration("http://localhost:8001", "us-west-2");
    AmazonS3Client client = AmazonS3ClientBuilder
      .standard()
      .withPathStyleAccessEnabled(true)  
      .withEndpointConfiguration(endpoint)
      .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))     
      .build();

    client.createBucket("testbucket");
    client.putObject("testbucket", "file/name", "contents");
    api.shutdown(); // kills the underlying actor system. Use api.stop() to just unbind the port.
```

Scala with AWS S3 SDK:
```scala
    import com.amazonaws.auth.AWSStaticCredentialsProvider
    import com.amazonaws.auth.AnonymousAWSCredentials
    import com.amazonaws.client.builder.AwsClientBuilder
    import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
    import com.amazonaws.services.s3.AmazonS3
    import com.amazonaws.services.s3.AmazonS3Builder
    import com.amazonaws.services.s3.AmazonS3Client
    import com.amazonaws.services.s3.AmazonS3ClientBuilder
    import io.findify.s3mock.S3Mock

    
    /** Create and start S3 API mock. */
    val api = S3Mock(port = 8001, dir = "/tmp/s3")
    api.start

    /* AWS S3 client setup.
     *  withPathStyleAccessEnabled(true) trick is required to overcome S3 default 
     *  DNS-based bucket access scheme
     *  resulting in attempts to connect to addresses like "bucketname.localhost"
     *  which requires specific DNS setup.
     */
    val endpoint = new EndpointConfiguration("http://localhost:8001", "us-west-2")
    val client = AmazonS3ClientBuilder
      .standard
      .withPathStyleAccessEnabled(true)  
      .withEndpointConfiguration(endpoint)
      .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))     
      .build

    /** Use it as usual. */
    client.createBucket("foo")
    client.putObject("foo", "bar", "baz")
    api.shutdown() // this one terminates the actor system. Use api.stop() to just unbind the service without messing with the ActorSystem
```

Scala with Pekko Connectors 1.0:
```scala
    import org.apache.pekko.actor.ActorSystem
    import org.apache.pekko.stream.ActorMaterializer
    import org.apache.pekko.stream.connectors.s3.scaladsl.S3
    import org.apache.pekko.stream.scaladsl.Sink
    import com.typesafe.config.ConfigFactory
    import scala.collection.JavaConverters._

    val config = ConfigFactory.parseMap(Map(
      "pekko.connectors.s3.proxy.host" -> "localhost",
      "pekko.connectors.s3.proxy.port" -> 8001,
      "pekko.connectors.s3.proxy.secure" -> false,
      "pekko.connectors.s3.path-style-access" -> true,
      "pekko.connectors.s3.aws.credentials.provider" -> "static",
      "pekko.connectors.s3.aws.credentials.access-key-id" -> "foo",
      "pekko.connectors.s3.aws.credentials.secret-access-key" -> "bar",
      "pekko.connectors.s3.aws.region.provider" -> "static",
      "pekko.connectors.s3.aws.region.default-region" -> "us-east-1"
    ).asJava)
    implicit val system = ActorSystem.create("test", config)
    implicit val mat = ActorMaterializer()
    import system.dispatcher
    val s3a = S3Client()
    val contents = s3a.download("bucket", "key")._1.runWith(Sink.reduce[ByteString](_ ++ _)).map(_.utf8String)
      
```

AWS CLI:
```bash
    export AWS_ACCESS_KEY_ID=foo
    export AWS_SECRET_ACCESS_KEY=dummy
    export AWS_SECRET_KEY=bar
    export AWS_REGION=eu-north-1
    export S3_ENDPOINT=http://localhost:8001

    aws s3api create-bucket --bucket my-bucket --endpoint-url=$S3_ENDPOINT

    aws s3api put-object --bucket my-bucket --key my-file --body ./my-file --endpoint-url=$S3_ENDPOINT

    aws s3api get-object --bucket my-bucket --key my-file --endpoint-url=$S3_ENDPOINT my-file-output
```


## License

The MIT License (MIT)

Copyright (c) 2016 Findify AB

Copyright (c) 2023 Marko Asplund

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
