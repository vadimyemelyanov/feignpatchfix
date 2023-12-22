package com.example.feignpatchfix

import com.example.feignpatchfix.api.TestApi
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class FeignPutFixApplication(private val testApi: TestApi) : CommandLineRunner {
    override fun run(vararg args: String) {
        testApi.testPatchRequest("test")
    }
}

fun main(args: Array<String>) {
    runApplication<FeignPutFixApplication>(*args)
}
