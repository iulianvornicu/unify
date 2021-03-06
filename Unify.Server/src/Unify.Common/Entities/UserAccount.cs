﻿using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Unify.Common.Interfaces;

namespace Unify.Common.Entities
{
    public class UserAccount : IUserAccount
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; }
        public string DisplayName { get; set; }
        public string Password { get; set; }
        public string Salt { get; set; }
        public string Email { get; set; }
        public IFacebookProfile FacebookProfile { get; set; }
        public ILinkedInProfile LinkedInProfile { get; set; }
        public ITwitterProfile TwitterProfile { get; set; }
    }
}
