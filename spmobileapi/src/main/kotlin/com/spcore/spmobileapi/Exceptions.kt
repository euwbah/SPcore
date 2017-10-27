package com.spcore.spmobileapi

class SPMobileAPIUninitializedException() :
        UnsupportedOperationException("SPMobileAPI can't be used until initialize() is called!!!")