package com.example.feignpatchfix.api

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody


@FeignClient(
    name = "testApi",
    url = "https://webhook.site/801e6f4f-e591-4a31-b55a-0c7fc362223b",
    primary = false
)
interface TestApi {
    @PatchMapping(value = ["/test"])
    fun testPatchRequest(
        @RequestBody request: Any
    ): String
}
