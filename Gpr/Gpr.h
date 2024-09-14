#pragma once

#include <cstdint>

class Gpr
{
public:
    Gpr() = default;
    ~Gpr() = default;

    static void process();

private:
    uint32_t _var = 0;
};
