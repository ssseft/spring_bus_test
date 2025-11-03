package com.example.bustest.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * 주소 값 객체
 */
@Embeddable
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Address {

    @Column(name = "zipcode")
    private String zipcode;

    @Column(name = "main_address")
    private String mainAddress;

    @Column(name = "detail_address")
    private String detailAddress;

}