﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Unify.Common.Interfaces;

namespace Unify.Api.ViewModels
{
    public class Register : IRegister
    {
        public IUserAccount UserAccount { get; set; }
        public string Password { get; set; }
    }
}
