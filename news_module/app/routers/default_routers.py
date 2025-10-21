from flask import Blueprint, request, current_app
from flask_jwt_extended import jwt_required, create_access_token
from ..utils.response import api_response
from ..handler import default_handler

default_bp = Blueprint('news', __name__)


@default_bp.route('/search', methods=['GET'])
def search():
    keyword = request.args.get('keyword', default='', type=str)
    from_param = request.args.get('from', default='', type=str)
    to_param = request.args.get('to', default='', type=str)
    limits = request.args.get('limit', default=100, type=int)
    sources = request.args.get('sources', default='', type=str)
    region = request.args.get('region', default='', type=str)
    success, res, msg = default_handler.search(keyword, from_param, to_param, limits, sources, region)
    if success:
        return api_response(code=200, data=res, message=msg)
    else:
        return api_response(code=500, data=res, message=msg)

@default_bp.route('/echo_test', methods=['POST'])
def echo_test():
    req = request.get_json()
    return api_response(code=200, data=req, message="echo")