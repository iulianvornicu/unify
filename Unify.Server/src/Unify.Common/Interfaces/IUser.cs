﻿using MongoDB.Bson;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Unify.Common.Interfaces
{
    public interface IUser : IUnifyEntity
    {
        string Username { get; set; }
        string Password { get; set; }
        string Salt { get; set; }
        string Email { get; set; }
    }
}
