package com.fashionstore.fashion_store.dto.momo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoExecuteResponseModel {
    private String orderId;
    private String amount;
    private String fullName;
    private String orderInfo;
}
