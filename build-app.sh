build_api_gateway="docker build -t local/training/auth -f api-gateway/Dockerfile ./api-gateway"
build_cart="docker build -t local/training/cart -f cart/Dockerfile ./cart"
build_member="docker build -t local/training/member -f member/Dockerfile ./member"
build_product="docker build -t local/training/product -f product/Dockerfile ./product"

eval "$build_api_gateway";eval "$build_cart";eval "$build_member";eval "$build_product";