/**
 * Plantky 后端根包。
 *
 * <p>Iteration 1 采用单体 Spring Boot 架构，但内部仍按照企业项目职责拆分：</p>
 * <pre>
 * controller -> service/orchestrator -> component -> mapper -> MySQL
 *                       |
 *                       +-> VO Response
 * </pre>
 */
package com.plantky;
