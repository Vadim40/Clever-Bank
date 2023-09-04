package org.CleverBank.Models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель банка, представляющая собой информацию о банке.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bank {
    /**
     * Уникальный идентификатор банка.
     */
    private int id;

    /**
     * Название банка.
     */
    private String name;
}
