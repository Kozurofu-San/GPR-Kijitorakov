
/*
 * Auto generated Run-Time-Environment Configuration File
 *      *** Do not modify ! ***
 *
 * Project: 'GPR' 
 * Target:  'GPR' 
 */

#ifndef RTE_COMPONENTS_H
#define RTE_COMPONENTS_H


/*
 * Define the Device Header File: 
 */
#define CMSIS_device_header "stm32f4xx.h"

/* ARM::RTOS&FreeRTOS:Config&FreeRTOS@10.5.1 */
#define RTE_RTOS_FreeRTOS_CONFIG        /* RTOS FreeRTOS Config for FreeRTOS API */
/* ARM::RTOS&FreeRTOS:Core&Cortex-M@10.5.1 */
#define RTE_RTOS_FreeRTOS_CORE          /* RTOS FreeRTOS Core */
/* ARM::RTOS&FreeRTOS:Heap&Heap_1@10.5.1 */
#define RTE_RTOS_FreeRTOS_HEAP_1        /* RTOS FreeRTOS Heap 1 */
/* ARM::RTOS&FreeRTOS:Timers@10.5.1 */
#define RTE_RTOS_FreeRTOS_TIMERS        /* RTOS FreeRTOS Timers */
/* Keil::Compiler&ARM Compiler:I/O:STDOUT&ITM@1.2.0 */
#define RTE_Compiler_IO_STDOUT          /* Compiler I/O: STDOUT */
          #define RTE_Compiler_IO_STDOUT_ITM      /* Compiler I/O: STDOUT ITM */
/* LVGL::LVGL&LVGL:lvgl:Essential@8.3.11 */
/*! \brief Enable LVGL */
#define RTE_GRAPHICS_LVGL
/* LVGL::LVGL&LVGL:lvgl:Extra Themes@8.3.11 */
/*! \brief use extra themes, widgets and layouts */
#define RTE_GRAPHICS_LVGL_USE_EXTRA_THEMES


#endif /* RTE_COMPONENTS_H */
